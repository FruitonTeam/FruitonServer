package cz.cuni.mff.fruiton.service.communication.chat;

import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dto.ChatProtos;

public interface ChatService {

    void accept(User from, ChatProtos.ChatMessage message);

}
