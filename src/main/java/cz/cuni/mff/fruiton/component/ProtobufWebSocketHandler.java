package cz.cuni.mff.fruiton.component;

import cz.cuni.mff.fruiton.config.WebSocketConfig;
import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.CommonProtos.Disconnected;
import cz.cuni.mff.fruiton.dto.CommonProtos.WrapperMessage;
import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.service.communication.CommunicationService;
import cz.cuni.mff.fruiton.service.communication.SessionService;
import cz.cuni.mff.fruiton.service.social.UserService;
import cz.cuni.mff.fruiton.service.util.UserStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class ProtobufWebSocketHandler extends BinaryWebSocketHandler {

    private static final int RECONNECT_CODE = 3500; // must be between 3000 and 5000

    @Value("${server.servlet.session.timeout}")
    private int defaultHttpSessionTimeout;

    private static final Logger logger = Logger.getLogger(ProtobufWebSocketHandler.class.getName());

    private final MessageDispatcher dispatcher;
    private final SessionService sessionService;

    private final UserService userService;

    private boolean applicationClosing = false;

    private final Object lock = new Object();

    private final UserStateService userStateService;

    private final CommunicationService communicationService;

    @Autowired
    public ProtobufWebSocketHandler(
            final MessageDispatcher dispatcher,
            final SessionService sessionService,
            final UserService userService,
            final UserStateService userStateService,
            final CommunicationService communicationService
    ) {
        this.dispatcher = dispatcher;
        this.sessionService = sessionService;
        this.userService = userService;
        this.userStateService = userStateService;
        this.communicationService = communicationService;
    }

    @PreDestroy
    private void onApplicationExit() {
        applicationClosing = true;
    }

    @Override
    protected final void handleBinaryMessage(final WebSocketSession session, final BinaryMessage message) {
        try {
            dispatcher.dispatch(session, message);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not convert message to protobuf object", e);
        }
    }

    @Override
    public final void afterConnectionEstablished(final WebSocketSession session) {
        logger.log(Level.FINE, "Opened connection for {0} with id: {1}",
                new Object[] {session.getPrincipal(), session.getId()});

        synchronized (lock) {
            if (!isReconnectWithPreviousConnectionOpen(session)) {
                logger.log(Level.FINEST, "Normal connect {0}", session);
                sessionService.register(session);
                sessionInit(session);
            } else {
                logger.log(Level.FINEST, "Reconnecting {0}", session);
                handleReconnect(session);
            }
        }
    }

    private boolean isReconnectWithPreviousConnectionOpen(final WebSocketSession session) {
        return sessionService.isOnline(session.getPrincipal());
    }

    private void sessionInit(final WebSocketSession session) {
        sendLoggedPlayerInfo(session);

        if (sessionService.hasOtherPlayersOnTheSameNetwork(session)) {
            sendPlayersOnTheSameNetworkInfo(session);
        }

        userStateService.setNewState(GameProtos.Status.MAIN_MENU, (UserIdHolder) session.getPrincipal());
    }

    private void handleReconnect(final WebSocketSession session) {
        WebSocketSession previousSession = sessionService.getSession(session.getPrincipal());
        boolean isNewLogin = !getToken(previousSession).equals(getToken(session));

        try {
            invalidateHttpSession(previousSession);
            if (previousSession.isOpen()) {
                logger.log(Level.FINE, "Sending Disconnected message to {0}", previousSession);
                communicationService.send(previousSession, WrapperMessage.newBuilder()
                        .setDisconnected(Disconnected.newBuilder())
                        .build());

                if (isNewLogin) { // let all services think that user went offline – closes games etc.
                    userStateService.setNewState(GameProtos.Status.OFFLINE, (UserIdHolder) session.getPrincipal());
                }
                previousSession.close(new CloseStatus(RECONNECT_CODE));
            } else {
                logger.log(Level.WARNING, "Server thinks that user tried to reconnect but his old connection is closed");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not close previous session", e);
        }

        sessionService.register(session);

        if (isNewLogin) {
            sessionInit(session);
        }
    }

    private String getToken(final WebSocketSession session) {
        return (String) session.getAttributes().get(WebSocketConfig.TOKEN_HEADER_KEY);
    }

    private void sendLoggedPlayerInfo(final WebSocketSession session) {
        WrapperMessage wrapperMessage = WrapperMessage.newBuilder()
                .setLoggedPlayerInfo(userService.getLoggedPlayerInfo((UserIdHolder) session.getPrincipal()))
                .build();

        communicationService.send(session, wrapperMessage);
    }

    private void sendPlayersOnTheSameNetworkInfo(final WebSocketSession session) {
        Set<WebSocketSession> otherPlayersSessions = sessionService.getOtherSessionsOnTheSameNetwork(session);

        communicationService.send(session, getPlayersOnlineMessage(
                otherPlayersSessions.stream().map(s -> s.getPrincipal().getName()).collect(Collectors.toSet())));

        BinaryMessage playerOnlineMsg = new BinaryMessage(
                getPlayersOnlineMessage(Collections.singleton(session.getPrincipal().getName())).toByteArray());
        for (WebSocketSession s : otherPlayersSessions) {
            communicationService.send(s, playerOnlineMsg);
        }
    }

    private WrapperMessage getPlayersOnlineMessage(final Set<String> onlinePlayers) {
        return WrapperMessage.newBuilder().setPlayersOnSameNetworkOnline(
                GameProtos.PlayersOnSameNetworkOnline.newBuilder()
                        .addAllLogins(onlinePlayers)
                        .build())
                .build();
    }

    @Override
    public final void afterConnectionClosed(final WebSocketSession session, final CloseStatus status) {
        if (status.getCode() == RECONNECT_CODE) { // ignore
            return;
        }

        logger.log(Level.FINE, "Closed connection for {0} with status: {1}", new Object[] {session.getPrincipal(), status});

        // if we were trying to send messages when application was closing then exceptions were thrown
        if (!applicationClosing) {
            synchronized (lock) {
                if (!getToken(sessionService.getSession(session.getPrincipal())).equals(getToken(session))) {
                    // this fixes bug when server and client closes the connection for reconnecting and client
                    // closes with different status code, so we would unregister new (reconnected) session
                    return;
                }

                try {
                    if (sessionService.hasOtherPlayersOnTheSameNetwork(session)) {
                        sendPlayerOnTheSameNetworkDisconnected(session);
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Could not send to other players on the network that player "
                            + session.getPrincipal() + " went offline", e);
                }

                try {
                    userStateService.setNewState(GameProtos.Status.OFFLINE, (UserIdHolder) session.getPrincipal());
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Could not set state to offline for " + session.getPrincipal(), e);
                }

                sessionService.unregister(session);

                // after WebSocket session is closed set its HttpSession timeout back to default value
                invalidateHttpSession(session);
            }
        }
    }

    private HttpSession getHttpSession(final WebSocketSession session) {
        return (HttpSession) session.getAttributes().get(WebSocketConfig.HTTP_SESSION_KEY);
    }

    private void invalidateHttpSession(final WebSocketSession session) {
        getHttpSession(session).setMaxInactiveInterval(defaultHttpSessionTimeout);
    }

    private void sendPlayerOnTheSameNetworkDisconnected(final WebSocketSession session) {
        BinaryMessage message = new BinaryMessage(getPlayerOfflineMessage(session.getPrincipal().getName()).toByteArray());

        for (WebSocketSession playerSession : sessionService.getOtherSessionsOnTheSameNetwork(session)) {
            communicationService.send(playerSession, message);
        }
    }

    private WrapperMessage getPlayerOfflineMessage(final String offlinePlayer) {
        return WrapperMessage.newBuilder().setPlayerOnSameNetworkOffline(
                GameProtos.PlayerOnSameNetworkOffline.newBuilder()
                        .setLogin(offlinePlayer)
                        .build())
                .build();
    }

}
