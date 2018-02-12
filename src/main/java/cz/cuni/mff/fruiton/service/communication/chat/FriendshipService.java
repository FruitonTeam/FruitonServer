package cz.cuni.mff.fruiton.service.communication.chat;

import cz.cuni.mff.fruiton.dao.UserIdHolder;

import java.util.List;

public interface FriendshipService {

    void addFriend(UserIdHolder user, String friendToAdd);

    List<String> getAllFriendRequests(UserIdHolder userIdHolder);

}
