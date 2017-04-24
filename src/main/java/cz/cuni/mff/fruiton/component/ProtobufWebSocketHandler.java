package cz.cuni.mff.fruiton.component;

import cz.cuni.mff.fruiton.service.communication.SessionService;
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

    @Autowired
    public ProtobufWebSocketHandler(MessageDispatcher dispatcher, SessionService sessionService) {
        this.dispatcher = dispatcher;
        this.sessionService = sessionService;
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        try {
            dispatcher.dispatch(session, message);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not convert message to protobuf object", e);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.log(Level.FINEST, "Opened connection: {0}", session);
        sessionService.register(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.log(Level.FINEST, "Closed connection: {0} with status: {1}", new Object[] {session, status});
        sessionService.unregister(session);
    }

}
