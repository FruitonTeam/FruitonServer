package cz.cuni.mff.fruiton.service.communication.chat;

import cz.cuni.mff.fruiton.dao.UserIdHolder;

import java.util.List;

public interface FriendshipService {

    /**
     * Sends friend request to player with name {@code friendToAdd} from {@code user}.
     * @param user user for whom to add a friend
     * @param friendToAdd login of the user who should be added among {@code user}'s friends
     */
    void addFriend(UserIdHolder user, String friendToAdd);

    /**
     * Gets all friend requests for specified user.
     * @param user user whose friend requests to fetch
     * @return logins of users who sent friend requests to {code user}
     */
    List<String> getAllFriendRequests(UserIdHolder user);

}
