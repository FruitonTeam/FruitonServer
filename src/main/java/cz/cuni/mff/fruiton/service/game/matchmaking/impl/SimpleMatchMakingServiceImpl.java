package cz.cuni.mff.fruiton.service.game.matchmaking.impl;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.GameProtos.FindGame;
import cz.cuni.mff.fruiton.dto.GameProtos.FruitonTeam;
import cz.cuni.mff.fruiton.service.game.GameService;
import cz.cuni.mff.fruiton.service.game.matchmaking.MatchMakingService;
import cz.cuni.mff.fruiton.util.KernelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@Profile("debug")
public final class SimpleMatchMakingServiceImpl implements MatchMakingService {

    private static final Logger logger = Logger.getLogger(SimpleMatchMakingServiceImpl.class.getName());

    private final Deque<UserIdHolder> waitingForOpponent = new LinkedList<>();

    private final Map<UserIdHolder, FruitonTeam> teams = new ConcurrentHashMap<>();

    private final GameService gameService;

    @Autowired
    public SimpleMatchMakingServiceImpl(final GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public synchronized void findGame(final UserIdHolder user, final FindGame findGameMsg) {
        if (!KernelUtils.isTeamValid(findGameMsg.getTeam())) {
            throw new IllegalArgumentException("Invalid team " + findGameMsg.getTeam());
        }

        Optional<UserIdHolder> opponent = getOpponent();
        if (opponent.isPresent()) {
            gameService.createGame(user, findGameMsg.getTeam(), opponent.get(), teams.remove(opponent.get()));
        } else {
            logger.log(Level.FINEST, "Adding {0} to waiting list", user);

            waitingForOpponent.add(user);

            teams.put(user, findGameMsg.getTeam());
        }
    }

    private Optional<UserIdHolder> getOpponent() {
        return Optional.ofNullable(waitingForOpponent.poll());
    }

    @Override
    public synchronized void removeFromMatchMaking(final UserIdHolder user) {
        if (waitingForOpponent.contains(user)) {
            logger.log(Level.FINE, "Removing {0} from matchmaking", user);
            waitingForOpponent.remove(user);
        }
    }

    @Override
    public void onDisconnected(final UserIdHolder user) {
        removeFromMatchMaking(user);
    }

}
