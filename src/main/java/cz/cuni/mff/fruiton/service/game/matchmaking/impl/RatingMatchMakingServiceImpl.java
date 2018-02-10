package cz.cuni.mff.fruiton.service.game.matchmaking.impl;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.dto.GameProtos.GameMode;
import cz.cuni.mff.fruiton.service.game.GameService;
import cz.cuni.mff.fruiton.service.game.matchmaking.MatchMakingService;
import cz.cuni.mff.fruiton.service.social.UserService;
import cz.cuni.mff.fruiton.service.util.UserStateService;
import cz.cuni.mff.fruiton.util.KernelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@Profile("production")
public final class RatingMatchMakingServiceImpl implements MatchMakingService {

    private static final int MATCH_REFRESH_TIME = 2000;

    private static final int LOW_PLAYERS_MODE_THRESHOLD = 10;

    private static final int DEFAULT_DELTA_WINDOW = 10;
    private static final int LOW_PLAYERS_MODE_DELTA_WINDOW = 100;

    private static final Logger logger = Logger.getLogger(RatingMatchMakingServiceImpl.class.getName());

    private final GameService gameService;

    private final UserService userService;

    private final UserStateService userStateService;

    private final Map<GameMode, TreeSet<WaitingUser>> waitingUsers = new HashMap<>();

    private final Map<UserIdHolder, GameProtos.FruitonTeam> teams = new ConcurrentHashMap<>();

    private boolean iterateAscending = false;

    @Autowired
    public RatingMatchMakingServiceImpl(
            final GameService gameService,
            final UserService userService,
            final UserStateService userStateService
    ) {
        this.gameService = gameService;
        this.userService = userService;
        this.userStateService = userStateService;

        for (GameMode gameMode : GameMode.values()) {
            waitingUsers.put(
                    gameMode,
                    new TreeSet<>((u1, u2) -> {
                        if (u1.getRating() != u2.getRating()) {
                            return Integer.compare(u1.getRating(), u2.getRating());
                        } else {
                            return u1.user.getUsername().compareTo(u2.user.getUsername());
                        }
                    })
            );
        }
    }

    @Override
    public synchronized void findGame(final UserIdHolder user, final GameProtos.FindGame findGameMsg) {
        if (!KernelUtils.isTeamValid(findGameMsg.getTeam())) {
            throw new IllegalArgumentException("Invalid team " + findGameMsg.getTeam());
        }

        logger.log(Level.FINEST, "Adding {0} to waiting list", user);

        userStateService.setNewState(UserStateService.UserState.IN_MATCHMAKING, user);

        teams.put(user, findGameMsg.getTeam());
        waitingUsers.get(findGameMsg.getGameMode()).add(new WaitingUser(user, userService.getRating(user)));
    }

    @Override
    public synchronized void removeFromMatchMaking(final UserIdHolder user) {
        remove(user);
        userStateService.setNewState(UserStateService.UserState.MAIN_MENU, user);
    }

    private void remove(final UserIdHolder user) {
        WaitingUser waitingUser = new WaitingUser(user, 0);
        for (TreeSet<WaitingUser> set : waitingUsers.values()) {
            if (set.contains(waitingUser)) {
                logger.log(Level.FINE, "Removing {0} from matchmaking", user);
                set.remove(waitingUser);
                break;
            }
        }
    }

    @Scheduled(fixedDelay = MATCH_REFRESH_TIME)
    public synchronized void match() {
        iterateAscending = !iterateAscending;

        for (Map.Entry<GameMode, TreeSet<WaitingUser>> waitingUserEntry : waitingUsers.entrySet()) {
            if (waitingUserEntry.getValue().isEmpty()) {
                continue;
            }

            matchWaitingUsers(waitingUserEntry);
            deleteMatchedUsers(waitingUserEntry.getValue());
        }
    }

    private void matchWaitingUsers(final Map.Entry<GameMode, TreeSet<WaitingUser>> waitingUserEntry) {
        Iterator<WaitingUser> it = getWaitingUsersIterator(waitingUserEntry.getValue());
        WaitingUser previous = it.next();
        while (it.hasNext()) {
            WaitingUser current = it.next();
            if (Math.abs(previous.getRating() - current.getRating()) < current.deltaWindow) { // it's a match
                gameService.createGame(
                        previous.user,
                        teams.get(previous.user),
                        current.user,
                        teams.get(current.user),
                        waitingUserEntry.getKey()
                );
                previous.markedForDelete = true;
                current.markedForDelete = true;

                if (!it.hasNext()) {
                    break;
                }
                previous = it.next();
            } else {
                previous.deltaWindow += getRatingDeltaWindow(waitingUserEntry.getValue());
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
    public void onDisconnected(final UserIdHolder user) {
        remove(user);
    }

    private static final class WaitingUser {

        private final UserIdHolder user;
        private final int rating;
        private int deltaWindow = DEFAULT_DELTA_WINDOW;
        private boolean markedForDelete = false;

        private WaitingUser(final UserIdHolder user) {
            this.user = user;
            this.rating = 0;
        }

        private WaitingUser(final UserIdHolder user, final int rating) {
            this.user = user;
            this.rating = rating;
        }

        private int getRating() {
            return rating;
        }

        @Override
        public boolean equals(final Object o) {
            return o instanceof WaitingUser && user.equals(o);
        }

        @Override
        public int hashCode() {
            return user.hashCode();
        }
    }

}
