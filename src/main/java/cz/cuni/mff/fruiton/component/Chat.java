package cz.cuni.mff.fruiton.component;

import cz.cuni.mff.fruiton.annotation.HandleProtobufMessage;
import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.ChatProtos;
import cz.cuni.mff.fruiton.dto.CommonProtos.WrapperMessage;
import cz.cuni.mff.fruiton.service.communication.chat.ChatService;
import cz.cuni.mff.fruiton.service.communication.chat.FriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public final class Chat {

    private static final Logger logger = Logger.getLogger(Chat.class.getName());

    private final ChatService chatService;

    private final FriendshipService friendshipService;

    @Autowired
    public Chat(final ChatService chatService, final FriendshipService friendshipService) {
        this.chatService = chatService;
        this.friendshipService = friendshipService;
    }

    @HandleProtobufMessage(messageCase = WrapperMessage.MessageCase.CHATMESSAGE)
    public void handleChatMessage(final UserIdHolder from, final ChatProtos.ChatMessage msg) {
        logger.log(Level.FINE, "Chat message received from {0} with content: {1}", new Object[] {from, msg});

        chatService.accept(from, msg);
    }

    @HandleProtobufMessage(messageCase = WrapperMessage.MessageCase.FRIENDREQUEST)
    public void handleFriendRequest(final UserIdHolder from, final ChatProtos.FriendRequest request) {
        logger.log(Level.FINER, "Received friend request from {0}: {1}", new Object[] {from, request});

        friendshipService.addFriend(from, request.getFriendToAdd());
    }

    @HandleProtobufMessage(messageCase = WrapperMessage.MessageCase.FRIENDREQUESTRESULT)
    public void handleFriendRequestResult(final UserIdHolder from, final ChatProtos.FriendRequestResult result) {
        logger.log(Level.FINER, "Received friendship request result {0} from {1}",
                new Object[] {from, result});

        friendshipService.handleFriendshipRequestResult(from, result);
    }

}
