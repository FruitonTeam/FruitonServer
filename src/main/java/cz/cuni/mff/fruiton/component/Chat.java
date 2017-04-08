package cz.cuni.mff.fruiton.component;

import cz.cuni.mff.fruiton.annotation.HandleProtobufMessage;
import cz.cuni.mff.fruiton.dto.UserProtos;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class Chat {

    private static final Logger logger = Logger.getLogger(Chat.class.getName());

    @HandleProtobufMessage(msgCase = UserProtos.WrapperMessage.MsgCase.CHAT)
    public void handleChatMessage(WebSocketSession session, UserProtos.ChatMsg chat) {
        logger.log(Level.FINE, "Chat message received from {0} with content: {1}", new Object[] {session, chat});
    }

}
