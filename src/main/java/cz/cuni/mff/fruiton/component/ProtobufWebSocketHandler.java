package cz.cuni.mff.fruiton.component;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.CommonProtos.WrapperMessage;
import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.service.communication.CommunicationService;
import cz.cuni.mff.fruiton.service.communication.SessionService;
import cz.cuni.mff.fruiton.service.social.UserService;
import cz.cuni.mff.fruiton.service.util.UserStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class ProtobufWebSocketHandler extends BinaryWebSocketHandler {

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
        logger.log(Level.FINEST, "Opened connection for {0} with id: {1}",
                new Object[] {session.getPrincipal(), session.getId()});

        synchronized (lock) {
            sessionService.register(session);

            sendLoggedPlayerInfo(session);

            if (sessionService.hasOtherPlayersOnTheSameNetwork(session)) {
                sendPlayersOnTheSameNetworkInfo(session);
            }

            userStateService.setNewState(GameProtos.Status.MAIN_MENU, (UserIdHolder) session.getPrincipal());
        }
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
        logger.log(Level.FINEST, "Closed connection for {0} with status: {1}", new Object[] {session.getPrincipal(), status});

        // if we were trying to send messages when application was closing then exceptions were thrown
        if (!applicationClosing) {
            synchronized (lock) {
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
            }
        }
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
