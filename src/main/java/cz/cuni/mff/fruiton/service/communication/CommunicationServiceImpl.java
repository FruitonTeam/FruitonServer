package cz.cuni.mff.fruiton.service.communication;

import cz.cuni.mff.fruiton.dto.CommonProtos.WrapperMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class CommunicationServiceImpl implements CommunicationService {

    private static final Logger logger = Logger.getLogger(CommunicationServiceImpl.class.getName());

    private final SessionService sessionService;

    @Autowired
    public CommunicationServiceImpl(final SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public final void send(final Principal principal, final WrapperMessage message) {
        logger.log(Level.FINEST, "Sending message {0} to user {1}", new Object[] {message, principal});

        BinaryMessage msg = new BinaryMessage(message.toByteArray());

        WebSocketSession session = sessionService.getSession(principal);
        if (session == null) {
            logger.log(Level.WARNING, "Cannot send message because connection was lost with {0}", principal);
            return;
        }
        try {
            session.sendMessage(msg);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Cannot send websocket message", e);
        }
    }

}
