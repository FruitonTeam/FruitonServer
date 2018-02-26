package cz.cuni.mff.fruiton.component;

import cz.cuni.mff.fruiton.annotation.ProtobufMessage;
import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.CommonProtos;
import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.dto.GameProtos.GameMode;
import cz.cuni.mff.fruiton.dto.GameProtos.PickMode;
import cz.cuni.mff.fruiton.dto.GameProtos.Status;
import cz.cuni.mff.fruiton.service.game.GameService;
import cz.cuni.mff.fruiton.service.game.matchmaking.TeamDraftService;
import cz.cuni.mff.fruiton.service.social.UserService;
import cz.cuni.mff.fruiton.service.util.UserStateService;
import cz.cuni.mff.fruiton.service.util.UserStateService.OnUserStateChangedListener;
import cz.cuni.mff.fruiton.util.FruitonTeamUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@Profile("production")
public final class RatingMatchMaking implements OnUserStateChangedListener {

    private static final int MATCH_REFRESH_TIME = 2000;

    private static final int LOW_PLAYERS_MODE_THRESHOLD = 10;

    private static final int DEFAULT_DELTA_WINDOW = 10;
    private static final int LOW_PLAYERS_MODE_DELTA_WINDOW = 100;

    private static final Logger logger = Logger.getLogger(RatingMatchMaking.class.getName());

    private static final Comparator<WaitingUser> USER_COMPARATOR = (u1, u2) -> {
        if (u1.getRating() != u2.getRating()) {
            return Integer.compare(u1.getRating(), u2.getRating());
        } else {
            return u1.user.getUsername().compareTo(u2.user.getUsername());
        }
    };

    private final GameService gameService;

    private final UserService userService;

    private final UserStateService userStateService;

    private final TeamDraftService draftService;

    private final Map<PickMode, Map<GameMode, TreeSet<WaitingUser>>> waitingUsers = new HashMap<>();

    private final Map<UserIdHolder, GameProtos.FruitonTeam> teams = new HashMap<>();

    private final Set<UserIdHolder> usersInThisMatchmaking = ConcurrentHashMap.newKeySet();

    private boolean iterateAscending = false;

    private final Object lock = new Object();

    @Autowired
    public RatingMatchMaking(
            final GameService gameService,
            final UserService userService,
            final UserStateService userStateService,
            final TeamDraftService draftService
    ) {
        this.gameService = gameService;
        this.userService = userService;
        this.userStateService = userStateService;
        this.draftService = draftService;

        for (PickMode pickMode : PickMode.values()) {
            for (GameMode gameMode : GameMode.values()) {
                waitingUsers.computeIfAbsent(pickMode, key -> new HashMap<>()).put(gameMode, new TreeSet<>(USER_COMPARATOR));
            }
        }
    }

    @PostConstruct
    private void init() {
        userStateService.addListener(this);
    }

    @ProtobufMessage(messageCase = CommonProtos.WrapperMessage.MessageCase.FINDGAME)
    public void findGame(final UserIdHolder user, final GameProtos.FindGame findGameMsg) {
        if (usersInThisMatchmaking.contains(user)) {
            throw new IllegalArgumentException("User " + user + " already is in this matchmaking");
        }
        synchronized (lock) {
            if (findGameMsg.getPickMode() == PickMode.STANDARD_PICK) {
                FruitonTeamUtils.checkTeamValidity(user, findGameMsg.getTeam(), userService);
            }

            logger.log(Level.FINEST, "Adding {0} to waiting list", user);

            userStateService.setNewState(Status.IN_MATCHMAKING, user);

            PickMode pickMode = findGameMsg.getPickMode();
            if (pickMode == PickMode.STANDARD_PICK) {
                teams.put(user, findGameMsg.getTeam());
            }

            usersInThisMatchmaking.add(user);

            TreeSet<WaitingUser> users = waitingUsers.get(pickMode).get(findGameMsg.getGameMode());
            users.add(new WaitingUser(user, userService.getRating(user)));
        }
    }

    @ProtobufMessage(messageCase = CommonProtos.WrapperMessage.MessageCase.CANCELFINDINGGAME)
    public void removeFromMatchMaking(final UserIdHolder user) {
        synchronized (lock) {
            remove(user);
            userStateService.setNewState(Status.MAIN_MENU, user);
        }
    }

    private void remove(final UserIdHolder user) {
        logger.log(Level.FINE, "Removing {0} from matchmaking", user);

        WaitingUser waitingUser = new WaitingUser(user, userService.getRating(user));
        for (PickMode pickMode : PickMode.values()) {
            for (TreeSet<WaitingUser> users : waitingUsers.get(pickMode).values()) {
                if (users.contains(waitingUser)) {
                    users.remove(waitingUser);
                    break;
                }
            }
        }
        teams.remove(user);
        usersInThisMatchmaking.remove(user);
    }

    @Scheduled(fixedDelay = MATCH_REFRESH_TIME)
    public void match() {
        synchronized (lock) {
            iterateAscending = !iterateAscending;

            for (PickMode pickMode : PickMode.values()) {
                for (GameMode gameMode : GameMode.values()) {
                    TreeSet<WaitingUser> users = waitingUsers.get(pickMode).get(gameMode);

                    if (users.isEmpty()) {
                        continue;
                    }

                    matchWaitingUsers(users, gameMode, pickMode);
                    deleteMatchedUsers(users);
                }
            }
        }
    }

    private void matchWaitingUsers(final TreeSet<WaitingUser> users, final GameMode gameMode, final PickMode pickMode) {
        Iterator<WaitingUser> it = getWaitingUsersIterator(users);
        WaitingUser previous = it.next();
        while (it.hasNext()) {
            WaitingUser current = it.next();
            if (Math.abs(previous.getRating() - current.getRating()) < current.deltaWindow) { // it's a match
                if (pickMode == PickMode.STANDARD_PICK) {
                    gameService.createGame(
                            previous.user,
                            teams.get(previous.user),
                            current.user,
                            teams.get(current.user),
                            gameMode
                    );

                    teams.remove(previous.user);
                    teams.remove(current.user);
                } else {
                    draftService.startDraft(previous.user, current.user, gameMode);
                }
                usersInThisMatchmaking.remove(previous.user);
                usersInThisMatchmaking.remove(current.user);

                previous.markedForDelete = true;
                current.markedForDelete = true;

                if (!it.hasNext()) {
                    break;
                }
                previous = it.next();
            } else {
                previous.deltaWindow += getRatingDeltaWindow(users);
                previous = current;
            }
        }
    }

    private int getRatingDeltaWindow(final TreeSet<WaitingUser> userSet) {
        if (userSet.size() < LOW_PLAYERS_MODE_THRESHOLD) {
            return LOW_PLAYERS_MODE_DELTA_WINDOW;
        } else {
            return DEFAULT_DELTA_WINDOW;
        }
    }

    private Iterator<WaitingUser> getWaitingUsersIterator(final TreeSet<WaitingUser> userSet) {
        Iterator<WaitingUser> it;
        if (iterateAscending) {
            it = userSet.iterator();
        } else {
            it = userSet.descendingIterator();
        }
        return it;
    }

    private void deleteMatchedUsers(final TreeSet<WaitingUser> userSet) {
        userSet.removeIf(user -> user.markedForDelete);
    }

    @Override
    public void onUserStateChanged(final UserIdHolder user, final Status newState) {
        if (!usersInThisMatchmaking.contains(user)) {
            return;
        }
        synchronized (lock) {
            if (newState == Status.OFFLINE) {
                remove(user);
            } else {
                logger.log(Level.SEVERE, "User's state changed to {0} even though he was in matchmaking: {1}",
                        new Object[]{newState, user});
            }
        }
    }

    private static final class WaitingUser {

        private final UserIdHolder user;
        private final int rating;
        private int deltaWindow = DEFAULT_DELTA_WINDOW;
        private boolean markedForDelete = false;

        private WaitingUser(final UserIdHolder user, final int rating) {
            this.user = user;
            this.rating = rating;
        }

        private int getRating() {
            return rating;
        }

        @Override
        public boolean equals(final Object o) {
            return o instanceof WaitingUser && user.equals(((WaitingUser) o).user);
        }

        @Override
        public int hashCode() {
            return user.hashCode();
        }
    }

}
