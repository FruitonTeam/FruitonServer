package cz.cuni.mff.fruiton.component;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.CommonProtos;
import cz.cuni.mff.fruiton.dto.GameProtos;
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

    @Autowired
    public ProtobufWebSocketHandler(
            final MessageDispatcher dispatcher,
            final SessionService sessionService,
            final UserService userService,
            final UserStateService userStateService
    ) {
        this.dispatcher = dispatcher;
        this.sessionService = sessionService;
        this.userService = userService;
        this.userStateService = userStateService;
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
    public final void afterConnectionEstablished(final WebSocketSession session) throws Exception {
        logger.log(Level.FINEST, "Opened connection for {0} with id: {1}",
                new Object[] {session.getPrincipal(), session.getId()});
        sessionService.register(session);

        synchronized (lock) {
            sendLoggedPlayerInfo(session);

            if (sessionService.hasOtherPlayersOnTheSameNetwork(session)) {
                sendPlayersOnTheSameNetworkInfo(session);
            }

            userStateService.setNewState(GameProtos.Status.MAIN_MENU, (UserIdHolder) session.getPrincipal());
        }
    }

    private void sendLoggedPlayerInfo(final WebSocketSession session) throws IOException {
        CommonProtos.WrapperMessage wrapperMessage = CommonProtos.WrapperMessage.newBuilder()
                .setLoggedPlayerInfo(userService.getLoggedPlayerInfo((UserIdHolder) session.getPrincipal()))
                .build();

        session.sendMessage(new BinaryMessage(wrapperMessage.toByteArray()));
    }

    private void sendPlayersOnTheSameNetworkInfo(final WebSocketSession session) throws IOException {
        Set<WebSocketSession> otherPlayersSessions = sessionService.getOtherSessionsOnTheSameNetwork(session);

        session.sendMessage(new BinaryMessage(getPlayersOnlineMessage(
                otherPlayersSessions.stream().map(s -> s.getPrincipal().getName()).collect(Collectors.toSet()))
                .toByteArray()));

        BinaryMessage playerOnlineMsg = new BinaryMessage(
                getPlayersOnlineMessage(Collections.singleton(session.getPrincipal().getName())).toByteArray());
        for (WebSocketSession s : otherPlayersSessions) {
            s.sendMessage(playerOnlineMsg);
        }
    }

    private CommonProtos.WrapperMessage getPlayersOnlineMessage(final Set<String> onlinePlayers) {
        return CommonProtos.WrapperMessage.newBuilder().setPlayersOnSameNetworkOnline(
                GameProtos.PlayersOnSameNetworkOnline.newBuilder()
                        .addAllLogins(onlinePlayers)
                        .build())
                .build();
    }

    @Override
    public final void afterConnectionClosed(final WebSocketSession session, final CloseStatus status) throws IOException {
        logger.log(Level.FINEST, "Closed connection for {0} with status: {1}", new Object[] {session.getPrincipal(), status});
        if (!applicationClosing) {
            synchronized (lock) {
                if (sessionService.hasOtherPlayersOnTheSameNetwork(session)) {
                    sendPlayerOnTheSameNetworkDisconnected(session);
                }

                userStateService.setNewState(GameProtos.Status.OFFLINE, (UserIdHolder) session.getPrincipal());

                sessionService.unregister(session);
            }
        }
    }

    private void sendPlayerOnTheSameNetworkDisconnected(final WebSocketSession session) throws IOException {
        BinaryMessage message = new BinaryMessage(getPlayerOfflineMessage(session.getPrincipal().getName()).toByteArray());

        for (WebSocketSession playerSession : sessionService.getOtherSessionsOnTheSameNetwork(session)) {
            playerSession.sendMessage(message);
        }
    }

    private CommonProtos.WrapperMessage getPlayerOfflineMessage(final String offlinePlayer) {
        return CommonProtos.WrapperMessage.newBuilder().setPlayerOnSameNetworkOffline(
                GameProtos.PlayerOnSameNetworkOffline.newBuilder()
                        .setLogin(offlinePlayer)
                        .build())
                .build();
    }

}
