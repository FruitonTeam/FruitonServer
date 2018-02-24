package cz.cuni.mff.fruiton.service.communication.chat;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.ChatProtos.ChatMessage;
import cz.cuni.mff.fruiton.dto.ChatProtos.ChatMessages;

public interface ChatService {

    /**
     * Delivers chat message.
     * @param from from whom should the message be delivered
     * @param message message data
     */
    void deliver(UserIdHolder from, ChatMessage message);

    /**
     * Returns messages between {@code user1} and {@code user2}.
     * @param user1 first user
     * @param user2 second user
     * @param page page of the query (0 returns first {@link ChatServiceImpl#MESSAGES_PAGE_SIZE} messages, etc.)
     * @return messages between specified users
     */
    ChatMessages getMessagesBetweenUsers(UserIdHolder user1, UserIdHolder user2, int page);

    /**
     * Returns messages delivered before message with id {@code messageId}. Returned mess
     * @param messageId id of the message
     * @param page page of the query (0 returns first {@link ChatServiceImpl#MESSAGES_PAGE_SIZE} messages, etc.)
     * @return messages before specified message
     */
    ChatMessages getMessagesBefore(String messageId, int page);

}
