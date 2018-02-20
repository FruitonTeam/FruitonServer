package cz.cuni.mff.fruiton.service.game.matchmaking.impl;

import cz.cuni.mff.fruiton.annotation.ProtobufMessage;
import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.CommonProtos.WrapperMessage.MessageCase;
import cz.cuni.mff.fruiton.dto.GameProtos.FindGame;
import cz.cuni.mff.fruiton.dto.GameProtos.FruitonTeam;
import cz.cuni.mff.fruiton.dto.GameProtos.GameMode;
import cz.cuni.mff.fruiton.dto.GameProtos.PickMode;
import cz.cuni.mff.fruiton.dto.GameProtos.Status;
import cz.cuni.mff.fruiton.service.game.GameService;
import cz.cuni.mff.fruiton.service.game.matchmaking.MatchMakingService;
import cz.cuni.mff.fruiton.service.game.matchmaking.TeamDraftService;
import cz.cuni.mff.fruiton.service.social.UserService;
import cz.cuni.mff.fruiton.service.util.UserStateService;
import cz.cuni.mff.fruiton.util.FruitonTeamUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
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

    private final Map<PickMode, Map<GameMode, Deque<UserIdHolder>>> waitingForOpponent = new HashMap<>();

    private final Map<UserIdHolder, FruitonTeam> teams = new ConcurrentHashMap<>();

    private final GameService gameService;

    private final UserStateService userStateService;

    private final TeamDraftService draftService;

    private final UserService userService;

    @Autowired
    public SimpleMatchMakingServiceImpl(
            final GameService gameService,
            final UserStateService userStateService,
            final TeamDraftService draftService,
            final UserService userService
    ) {
        this.gameService = gameService;
        this.userStateService = userStateService;
        this.draftService = draftService;
        this.userService = userService;

        for (PickMode pickMode : PickMode.values()) {
            for (GameMode gameMode : GameMode.values()) {
                waitingForOpponent.computeIfAbsent(pickMode, key -> new HashMap<>()).put(gameMode, new LinkedList<>());
            }
        }
    }

    @PostConstruct
    private void init() {
        userStateService.addListener(this);
    }

    @Override
    @ProtobufMessage(messageCase = MessageCase.FINDGAME)
    public synchronized void findGame(final UserIdHolder user, final FindGame findGameMsg) {
        if (findGameMsg.getPickMode() == PickMode.STANDARD_PICK) {
            FruitonTeamUtils.checkTeamValidity(user, findGameMsg.getTeam(), userService);
        }

        userStateService.setNewState(Status.IN_MATCHMAKING, user);

        PickMode pickMode = findGameMsg.getPickMode();
        GameMode gameMode = findGameMsg.getGameMode();

        Optional<UserIdHolder> opponent = getOpponent(pickMode, gameMode);
        if (opponent.isPresent()) {
            if (pickMode == PickMode.STANDARD_PICK) {
                gameService.createGame(user, findGameMsg.getTeam(), opponent.get(), teams.remove(opponent.get()), gameMode);
            } else {
                draftService.startDraft(user, opponent.get(), gameMode);
            }
        } else {
            logger.log(Level.FINEST, "Adding {0} to waiting list", user);

            waitingForOpponent.get(pickMode).get(gameMode).add(user);

            if (pickMode == PickMode.STANDARD_PICK) {
                teams.put(user, findGameMsg.getTeam());
            }
        }
    }

    private Optional<UserIdHolder> getOpponent(final PickMode pickMode, final GameMode gameMode) {
        return Optional.ofNullable(waitingForOpponent.get(pickMode).get(gameMode).poll());
    }

    @Override
    @ProtobufMessage(messageCase = MessageCase.CANCELFINDINGGAME)
    public synchronized void removeFromMatchMaking(final UserIdHolder user) {
        removeFromQueue(user);
        userStateService.setNewState(Status.MAIN_MENU, user);
    }

    private void removeFromQueue(final UserIdHolder user) {
        for (PickMode pickMode : PickMode.values()) {
            for (Deque<UserIdHolder> queue : waitingForOpponent.get(pickMode).values()) {
                if (queue.contains(user)) {
                    logger.log(Level.FINE, "Removing {0} from matchmaking", user);
                    queue.remove(user);
                }
            }
        }
    }

    @Override
    public void onUserStateChanged(final UserIdHolder user, final Status newState) {
        if (newState == Status.OFFLINE) {
            removeFromQueue(user);
        }
    }
}
