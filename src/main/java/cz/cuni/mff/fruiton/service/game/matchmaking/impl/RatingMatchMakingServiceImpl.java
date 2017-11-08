package cz.cuni.mff.fruiton.service.game.matchmaking.impl;

import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.service.game.GameService;
import cz.cuni.mff.fruiton.service.game.matchmaking.MatchMakingService;
import cz.cuni.mff.fruiton.util.KernelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
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

    private final TreeSet<WaitingUser> waitingUsers = new TreeSet<>((u1, u2) -> {
        if (u1.user.getRating() != u2.user.getRating()) {
            return Integer.compare(u1.user.getRating(), u2.user.getRating());
        } else {
            return u1.user.getLogin().compareTo(u2.user.getLogin());
        }
    });

    private final Map<User, GameProtos.FruitonTeam> teams = new Hashtable<>();

    private boolean iterateAscending = false;

    @Autowired
    public RatingMatchMakingServiceImpl(final GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public synchronized void findGame(final User user, final GameProtos.FindGame findGameMsg) {
        if (!KernelUtils.isTeamValid(findGameMsg.getTeam())) {
            throw new IllegalArgumentException("Invalid team " + findGameMsg.getTeam());
        }

        user.setState(User.State.MATCHMAKING);

        logger.log(Level.FINEST, "Adding {0} to waiting list", user);

        teams.put(user, findGameMsg.getTeam());
        waitingUsers.add(new WaitingUser(user));
    }

    @Override
    public synchronized void removeFromMatchMaking(final User user) {
        logger.log(Level.FINE, "Removing {0} from matchmaking", user);

        WaitingUser waitingUser = new WaitingUser(user);
        if (waitingUsers.contains(waitingUser)) {
            waitingUsers.remove(waitingUser);
        } else {
            logger.log(Level.WARNING, "Could not remove user {0} from match making", user);
        }
    }

    @Scheduled(fixedDelay = MATCH_REFRESH_TIME)
    public synchronized void match() {
        if (waitingUsers.isEmpty()) {
            return;
        }

        iterateAscending = !iterateAscending;
        matchWaitingUsers();
        deleteMatchedUsers();
    }

    private void matchWaitingUsers() {
        Iterator<WaitingUser> it = getWaitingUsersIterator();
        WaitingUser previous = it.next();
        while (it.hasNext()) {
            WaitingUser current = it.next();
            if (Math.abs(previous.user.getRating() - current.user.getRating()) < current.deltaWindow) { // it's a match
                gameService.createGame(previous.user, teams.get(previous.user), current.user, teams.get(current.user));
                previous.markedForDelete = true;
                current.markedForDelete = true;

                if (!it.hasNext()) {
                    break;
                }
                previous = it.next();
            } else {
                previous.deltaWindow += getRatingDeltaWindow();
                previous = current;
            }
        }
    }

    private int getRatingDeltaWindow() {
        if (waitingUsers.size() < LOW_PLAYERS_MODE_THRESHOLD) {
            return LOW_PLAYERS_MODE_DELTA_WINDOW;
        } else {
            return DEFAULT_DELTA_WINDOW;
        }
    }

    private Iterator<WaitingUser> getWaitingUsersIterator() {
        Iterator<WaitingUser> it;
        if (iterateAscending) {
            it = waitingUsers.iterator();
        } else {
            it = waitingUsers.descendingIterator();
        }
        return it;
    }

    private void deleteMatchedUsers() {
        waitingUsers.removeIf(user -> user.markedForDelete);
    }

    private static final class WaitingUser {

        private final User user;
        private int deltaWindow = DEFAULT_DELTA_WINDOW;
        private boolean markedForDelete = false;

        private WaitingUser(final User user) {
            this.user = user;
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
