package cz.cuni.mff.fruiton.component;

import cz.cuni.mff.fruiton.annotation.HandleProtobufMessage;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dto.ChatProtos;
import cz.cuni.mff.fruiton.dto.CommonProtos.WrapperMessage;
import cz.cuni.mff.fruiton.service.communication.chat.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class Chat {

    private static final Logger logger = Logger.getLogger(Chat.class.getName());

    private final ChatService chatService;

    @Autowired
    public Chat(final ChatService chatService) {
        this.chatService = chatService;
    }

    @HandleProtobufMessage(messageCase = WrapperMessage.MessageCase.CHATMESSAGE)
    public final void handleChatMessage(final User from, final ChatProtos.ChatMessage msg) {
        logger.log(Level.FINE, "Chat message received from {0} with content: {1}", new Object[] {from, msg});

        chatService.accept(from, msg);
    }

}
