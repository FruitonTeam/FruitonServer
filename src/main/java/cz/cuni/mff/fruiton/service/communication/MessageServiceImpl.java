package cz.cuni.mff.fruiton.service.communication;

import cz.cuni.mff.fruiton.dto.GameProtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;

import java.io.IOException;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger logger = Logger.getLogger(MessageServiceImpl.class.getName());

    private final SessionService sessionService;

    @Autowired
    public MessageServiceImpl(final SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public final void send(final Principal principal, final GameProtos.WrapperMessage message) {
        BinaryMessage msg = new BinaryMessage(message.toByteArray());

        try {
            sessionService.getSession(principal).sendMessage(msg);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Cannot send websocket message", e);
        }
    }

}
