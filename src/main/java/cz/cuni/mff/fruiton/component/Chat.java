package cz.cuni.mff.fruiton.component;

import cz.cuni.mff.fruiton.annotation.HandleProtobufMessage;
import cz.cuni.mff.fruiton.dao.model.User;
import cz.cuni.mff.fruiton.dto.UserProtos;
import cz.cuni.mff.fruiton.service.communication.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class Chat {

    private static final Logger logger = Logger.getLogger(Chat.class.getName());

    @Autowired
    private MessageService msgService;

    @HandleProtobufMessage(msgCase = UserProtos.WrapperMessage.MsgCase.CHAT)
    public void handleChatMessage(User user, UserProtos.ChatMsg chat) {
        logger.log(Level.FINE, "Chat message received from {0} with content: {1}", new Object[] {user, chat});

        UserProtos.WrapperMessage m = UserProtos.WrapperMessage.newBuilder()
                .setChat(UserProtos.ChatMsg.newBuilder()
                        .setMsg("lololo")
                        .build())
                .build();

        msgService.send(user, m);
    }

}
