package cz.cuni.mff.fruiton.service.communication.chat;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.ChatProtos;

public interface ChatService {

    void accept(UserIdHolder from, ChatProtos.ChatMessage message);

}
