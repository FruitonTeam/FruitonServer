package cz.cuni.mff.fruiton.service.communication.chat;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.ChatProtos;

import java.util.List;

public interface FriendshipService {

    void addFriend(UserIdHolder user, String friendToAdd);

    void handleFriendshipRequestResult(UserIdHolder from, ChatProtos.FriendRequestResult result);

    List<String> getAllFriendRequests(UserIdHolder userIdHolder);

}
