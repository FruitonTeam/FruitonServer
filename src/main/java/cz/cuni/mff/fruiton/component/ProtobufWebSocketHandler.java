package cz.cuni.mff.fruiton.component;

import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.service.communication.SessionService;
import cz.cuni.mff.fruiton.service.game.GameService;
import cz.cuni.mff.fruiton.service.game.MatchMakingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class ProtobufWebSocketHandler extends BinaryWebSocketHandler {

    private static final Logger logger = Logger.getLogger(ProtobufWebSocketHandler.class.getName());

    private final MessageDispatcher dispatcher;
    private final SessionService sessionService;

    private final MatchMakingService matchMakingService;
    private final GameService gameService;

    @Autowired
    public ProtobufWebSocketHandler(
            final MessageDispatcher dispatcher,
            final SessionService sessionService,
            final MatchMakingService matchMakingService,
            final GameService gameService
    ) {
        this.dispatcher = dispatcher;
        this.sessionService = sessionService;
        this.matchMakingService = matchMakingService;
        this.gameService = gameService;
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
    }

    @Override
    public final void afterConnectionClosed(final WebSocketSession session, final CloseStatus status) throws Exception {
        logger.log(Level.FINEST, "Closed connection for {0} with status: {1}", new Object[] {session.getPrincipal(), status});
        sessionService.unregister(session);

        User user = (User) session.getPrincipal();
        if (user.getState() == User.State.MATCHMAKING) {
            matchMakingService.removeFromMatchMaking(user);
        } else if (user.getState() == User.State.IN_GAME) {
            gameService.userDisconnected(user);
        }
    }

}
