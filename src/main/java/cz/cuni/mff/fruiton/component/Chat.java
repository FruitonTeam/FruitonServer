package cz.cuni.mff.fruiton.component;

import cz.cuni.mff.fruiton.annotation.HandleProtobufMessage;
import cz.cuni.mff.fruiton.dto.UserProtos;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class Chat {

    @HandleProtobufMessage(msgCase = UserProtos.WrapperMessage.MsgCase.CHAT)
    public void handleChatMessage(WebSocketSession session, UserProtos.ChatMsg chat) {
        System.out.println(session + ": chat msg received " + chat.getMsg());
    }

}
