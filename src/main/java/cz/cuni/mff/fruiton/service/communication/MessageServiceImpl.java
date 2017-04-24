package cz.cuni.mff.fruiton.service.communication;

import cz.cuni.mff.fruiton.dto.UserProtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger logger = Logger.getLogger(MessageServiceImpl.class.getName());

    private final SessionService sessionService;

    @Autowired
    public MessageServiceImpl(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public void send(Principal principal, UserProtos.WrapperMessage message) {
        BinaryMessage msg = new BinaryMessage(message.toByteArray());

        TextMessage txt = new TextMessage("trololo");

        try {
            sessionService.getSession(principal).sendMessage(txt);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Cannot send websocket message", e);
        }
    }

}
