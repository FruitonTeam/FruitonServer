package cz.cuni.mff.fruiton.service.game.matchmaking.impl;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.GameProtos.FindGame;
import cz.cuni.mff.fruiton.dto.GameProtos.FruitonTeam;
import cz.cuni.mff.fruiton.dto.GameProtos.GameMode;
import cz.cuni.mff.fruiton.service.game.GameService;
import cz.cuni.mff.fruiton.service.game.matchmaking.MatchMakingService;
import cz.cuni.mff.fruiton.service.util.UserStateService;
import cz.cuni.mff.fruiton.util.KernelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Deque;
import java.util.HashMap;
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

    private final Map<GameMode, Deque<UserIdHolder>> waitingForOpponent = new HashMap<>();

    private final Map<UserIdHolder, FruitonTeam> teams = new ConcurrentHashMap<>();

    private final GameService gameService;

    private final UserStateService userStateService;

    @Autowired
    public SimpleMatchMakingServiceImpl(final GameService gameService, final UserStateService userStateService) {
        this.gameService = gameService;
        this.userStateService = userStateService;
        for (GameMode gameMode : GameMode.values()) {
            waitingForOpponent.put(gameMode, new LinkedList<>());
        }
    }

    @Override
    public synchronized void findGame(final UserIdHolder user, final FindGame findGameMsg) {
        if (!KernelUtils.isTeamValid(findGameMsg.getTeam())) {
            throw new IllegalArgumentException("Invalid team " + findGameMsg.getTeam());
        }

        userStateService.setNewState(UserStateService.UserState.IN_MATCHMAKING, user);

        GameMode gameMode = findGameMsg.getGameMode();

        Optional<UserIdHolder> opponent = getOpponent(gameMode);
        if (opponent.isPresent()) {
            gameService.createGame(user, findGameMsg.getTeam(), opponent.get(), teams.remove(opponent.get()), gameMode);
        } else {
            logger.log(Level.FINEST, "Adding {0} to waiting list", user);

            waitingForOpponent.get(gameMode).add(user);

            teams.put(user, findGameMsg.getTeam());
        }
    }

    private Optional<UserIdHolder> getOpponent(final GameMode gameMode) {
        return Optional.ofNullable(waitingForOpponent.get(gameMode).poll());
    }

    @Override
    public synchronized void removeFromMatchMaking(final UserIdHolder user) {
        removeFromQueue(user);
        userStateService.setNewState(UserStateService.UserState.MAIN_MENU, user);
    }

    private void removeFromQueue(final UserIdHolder user) {
        for (Deque<UserIdHolder> queue : waitingForOpponent.values()) {
            if (queue.contains(user)) {
                logger.log(Level.FINE, "Removing {0} from matchmaking", user);
                queue.remove(user);
            }
        }
    }

    @Override
    public void onDisconnected(final UserIdHolder user) {
        removeFromQueue(user);
    }

}
