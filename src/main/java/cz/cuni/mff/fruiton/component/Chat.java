package cz.cuni.mff.fruiton.component;

import cz.cuni.mff.fruiton.annotation.HandleProtobufMessage;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dto.ChatProtos;
import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.service.communication.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class Chat {

    private static final Logger logger = Logger.getLogger(Chat.class.getName());

    private final MessageService msgService;

    @Autowired
    public Chat(final MessageService msgService) {
        this.msgService = msgService;
    }

    @HandleProtobufMessage(msgCase = GameProtos.WrapperMessage.MsgCase.CHATMSG)
    public final void handleChatMessage(final User user, final ChatProtos.ChatMsg chat) {
        logger.log(Level.FINE, "Chat message received from {0} with content: {1}", new Object[] {user, chat});

        GameProtos.WrapperMessage m = GameProtos.WrapperMessage.newBuilder()
                .setChatMsg(ChatProtos.ChatMsg.newBuilder()
                        .setMsg("lololo")
                        .build())
                .build();

        msgService.send(user, m);
    }

}
