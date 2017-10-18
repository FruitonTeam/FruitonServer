package cz.cuni.mff.fruiton.service.game.impl;

import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dto.GameProtos.FindGame;
import cz.cuni.mff.fruiton.dto.GameProtos.FruitonTeam;
import cz.cuni.mff.fruiton.service.game.GameService;
import cz.cuni.mff.fruiton.service.game.MatchMakingService;
import cz.cuni.mff.fruiton.util.KernelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Deque;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public final class SimpleMatchMakingServiceImpl implements MatchMakingService {

    private static final Logger logger = Logger.getLogger(SimpleMatchMakingServiceImpl.class.getName());

    private final Deque<User> waitingForOpponent = new LinkedList<>();

    private final Map<User, FruitonTeam> teams = new Hashtable<>();

    private final GameService gameService;

    @Autowired
    public SimpleMatchMakingServiceImpl(final GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public synchronized void findGame(final User user, final FindGame findGameMsg) {
        if (!KernelUtils.isTeamValid(findGameMsg.getTeam())) {
            throw new IllegalArgumentException("Invalid team " + findGameMsg.getTeam());
        }

        user.setState(User.State.MATCHMAKING);

        Optional<User> opponent = getOpponent(user);
        if (opponent.isPresent()) {
            gameService.createGame(user, findGameMsg.getTeam(), opponent.get(), teams.remove(opponent.get()));
        } else {
            logger.log(Level.FINEST, "Adding {0} to waiting list", user);

            waitingForOpponent.add(user);

            teams.put(user, findGameMsg.getTeam());
        }
    }

    private Optional<User> getOpponent(final User user) {
        return Optional.ofNullable(waitingForOpponent.poll());
    }

    @Override
    public synchronized void removeFromMatchMaking(final User user) {
        logger.log(Level.FINE, "Removing {0} from matchmaking", user);

        user.setState(User.State.MENU);

        if (waitingForOpponent.contains(user)) {
            waitingForOpponent.remove(user);
        } else {
            logger.log(Level.WARNING, "Could not remove user {0} from match making", user);
        }
    }

}
