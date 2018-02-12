package cz.cuni.mff.fruiton.service.communication.chat;

import cz.cuni.mff.fruiton.annotation.ProtobufMessage;
import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dao.domain.FriendRequest;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dao.repository.FriendRequestRepository;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.dto.ChatProtos;
import cz.cuni.mff.fruiton.dto.ChatProtos.FriendRequestResult;
import cz.cuni.mff.fruiton.dto.CommonProtos;
import cz.cuni.mff.fruiton.dto.CommonProtos.WrapperMessage.MessageCase;
import cz.cuni.mff.fruiton.service.communication.CommunicationService;
import cz.cuni.mff.fruiton.service.communication.SessionService;
import cz.cuni.mff.fruiton.service.social.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public final class FriendshipServiceImpl implements FriendshipService {

    private static final String FRIEND_ADDED_NOTIF_HEADER = "Friend added";

    private static final Logger logger = Logger.getLogger(FriendshipServiceImpl.class.getName());

    private final CommunicationService communicationService;

    private final UserRepository userRepository;

    private final FriendRequestRepository friendRequestRepository;

    private final SessionService sessionService;

    private final UserService userService;

    @Autowired
    public FriendshipServiceImpl(
            final CommunicationService communicationService,
            final UserRepository userRepository,
            final FriendRequestRepository friendRequestRepository,
            final SessionService sessionService,
            final UserService userService
    ) {
        this.communicationService = communicationService;
        this.userRepository = userRepository;
        this.friendRequestRepository = friendRequestRepository;
        this.sessionService = sessionService;
        this.userService = userService;
    }

    @ProtobufMessage(messageCase = MessageCase.FRIENDREQUEST)
    private void handleFriendRequest(final UserIdHolder from, final ChatProtos.FriendRequest request) {
        logger.log(Level.FINER, "Received friend request from {0}: {1}", new Object[] {from, request});

        this.addFriend(from, request.getFriendToAdd());
    }

    @Override
    public void addFriend(final UserIdHolder user, final String friendToAdd) {
        User friend = userRepository.findByLogin(friendToAdd);

        User from = userRepository.findOne(user.getId());
        if (friendRequestRepository.existsByFromAndTo(from, friend)) {
            // friend request already exists and the recipient did not respond yet
            return;
        }

        if (friendRequestRepository.existsByFromAndTo(friend, from)) {
            // friend request was sent the other way around so ignore for now because more complications arise
            return;
        }

        FriendRequest request = new FriendRequest(from, friend);
        friendRequestRepository.save(request);

        if (sessionService.isOnline(UserIdHolder.of(friend))) {
            communicationService.send(UserIdHolder.of(friend), CommonProtos.WrapperMessage.newBuilder()
                    .setFriendRequest(ChatProtos.FriendRequest.newBuilder().setFriendToAdd(user.getUsername()))
                    .build());
        }
    }

    @ProtobufMessage(messageCase = MessageCase.FRIENDREQUESTRESULT)
    private void handleFriendshipRequestResult(final UserIdHolder from, final FriendRequestResult result) {
        logger.log(Level.FINER, "Received friendship request result {0} from {1}",
                new Object[] {from, result});

        User friend = userRepository.findByLogin(result.getFriendToAdd());

        FriendRequest request = friendRequestRepository.findByFromAndTo(friend, userRepository.findOne(from.getId()));

        if (result.getFriendshipAccepted()) {
            friendshipAccepted(request);
        }

        friendRequestRepository.delete(request);
    }

    private void friendshipAccepted(final FriendRequest request) {
        if (sessionService.isOnline(UserIdHolder.of(request.getFrom()))) {
            // we send notification and result separately
            // (it is possible to derive result on client from notification but it seemed counter-intuitive)
            communicationService.sendNotification(UserIdHolder.of(request.getFrom()),
                    userService.getBase64Avatar(request.getTo().getLogin()).orElse(""),
                    FRIEND_ADDED_NOTIF_HEADER, request.getTo().getLogin());
            communicationService.send(UserIdHolder.of(request.getFrom()), CommonProtos.WrapperMessage.newBuilder()
                    .setFriendRequestResult(
                            FriendRequestResult.newBuilder()
                                    .setFriendToAdd(request.getTo().getLogin())
                                    .setFriendshipAccepted(true))
                    .build());
        }

        request.getFrom().addFriend(request.getTo());
        userRepository.save(request.getFrom());

        request.getTo().addFriend(request.getFrom());
        userRepository.save(request.getTo());
    }

    @Override
    public List<String> getAllFriendRequests(final UserIdHolder userIdHolder) {
        User user = userRepository.findOne(userIdHolder.getId());

        List<FriendRequest> requests = friendRequestRepository.findByTo(user);
        if (requests == null) {
            return Collections.emptyList();
        }

        return requests.stream().map(req -> req.getFrom().getLogin()).collect(Collectors.toList());
    }

}
