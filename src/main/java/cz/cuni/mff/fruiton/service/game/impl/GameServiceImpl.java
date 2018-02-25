package cz.cuni.mff.fruiton.service.game.impl;

import cz.cuni.mff.fruiton.annotation.ProtobufMessage;
import cz.cuni.mff.fruiton.component.AchievementHelper;
import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dao.domain.Achievement;
import cz.cuni.mff.fruiton.dto.CommonProtos.WrapperMessage;
import cz.cuni.mff.fruiton.dto.CommonProtos.WrapperMessage.MessageCase;
import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.dto.GameProtos.FruitonTeam;
import cz.cuni.mff.fruiton.dto.GameProtos.GameMode;
import cz.cuni.mff.fruiton.dto.GameProtos.GameOver;
import cz.cuni.mff.fruiton.dto.GameProtos.GameOver.Reason;
import cz.cuni.mff.fruiton.dto.GameProtos.GameRewards;
import cz.cuni.mff.fruiton.dto.GameProtos.Quest;
import cz.cuni.mff.fruiton.dto.GameProtos.Status;
import cz.cuni.mff.fruiton.service.communication.CommunicationService;
import cz.cuni.mff.fruiton.service.game.AchievementService;
import cz.cuni.mff.fruiton.service.game.FruitonService;
import cz.cuni.mff.fruiton.service.game.GameResult;
import cz.cuni.mff.fruiton.service.game.GameService;
import cz.cuni.mff.fruiton.service.game.quest.QuestService;
import cz.cuni.mff.fruiton.service.game.RatingService;
import cz.cuni.mff.fruiton.service.social.UserService;
import cz.cuni.mff.fruiton.service.util.UserStateService;
import cz.cuni.mff.fruiton.util.FruitonTeamUtils;
import cz.cuni.mff.fruiton.util.HaxeUtils;
import cz.cuni.mff.fruiton.util.KernelUtils;
import fruiton.kernel.Fruiton;
import fruiton.kernel.GameState;
import fruiton.kernel.Kernel;
import fruiton.kernel.Player;
import fruiton.kernel.actions.Action;
import fruiton.kernel.actions.EndTurnAction;
import fruiton.kernel.actions.MoveAction;
import fruiton.kernel.events.DeathEvent;
import fruiton.kernel.events.Event;
import fruiton.kernel.events.GameOverEvent;
import haxe.lang.HaxeException;
import haxe.root.Array;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public final class GameServiceImpl implements GameService {

    private static final int STANDARD_MONEY_REWARD = 50;

    private static final int TURN_TIME_CHECK_REFRESH_TIME = 1000;

    private static final int PLAYER_READY_WAIT_TIME_SEC = 15;

    private static final String WINNER_ACHIEVEMENT = "Winner";
    private static final String FIGHTER_ACHIEVEMENT = "Fighter";
    private static final String TENACIOUS_ACHIEVEMENT = "Tenacious";

    private static final Logger logger = Logger.getLogger(GameServiceImpl.class.getName());

    private static final AtomicInteger ATOMIC_INT = new AtomicInteger();

    private final Random random = new Random();

    private final Map<UserIdHolder, GameData> userToGameData = new HashMap<>();

    private final ReadWriteLock gamesLock = new ReentrantReadWriteLock();

    private final CommunicationService communicationService;

    private final AchievementService achievementService;

    private final AchievementHelper achievementHelper;

    private final UserService userService;

    private final FruitonService fruitonService;

    private final QuestService questService;

    private final UserStateService userStateService;

    private final RatingService ratingService;

    @Autowired
    public GameServiceImpl(
            final CommunicationService communicationService,
            final AchievementService achievementService,
            final AchievementHelper achievementHelper,
            final UserService userService,
            final FruitonService fruitonService,
            final QuestService questService,
            final UserStateService userStateService,
            final RatingService ratingService
    ) {
        this.communicationService = communicationService;
        this.achievementService = achievementService;
        this.achievementHelper = achievementHelper;
        this.userService = userService;
        this.fruitonService = fruitonService;
        this.questService = questService;
        this.userStateService = userStateService;
        this.ratingService = ratingService;
    }

    @PostConstruct
    private void init() {
        userStateService.addListener(this);
    }

    @Override
    public void createGame(
            final UserIdHolder user1,
            final FruitonTeam team1,
            final UserIdHolder user2,
            final FruitonTeam team2,
            final GameMode gameMode
    ) {
        logger.log(Level.FINE, "Creating game between {0} and {1} with teams {2} and {3}",
                new Object[] {user1, user2, team1, team2});

        Player player1 = new Player(ATOMIC_INT.getAndIncrement());
        Player player2 = new Player(ATOMIC_INT.getAndIncrement());

        boolean firstUserStartsFirst = random.nextBoolean();

        FruitonTeam finalTeam1 = team1;
        FruitonTeam finalTeam2 = team2;

        if (firstUserStartsFirst) {
            finalTeam2 = convertFruitonPositions(team2);
        } else {
            finalTeam1 = convertFruitonPositions(team1);
        }

        Array<Fruiton> fruitons = getFruitonsArray(player1, player2, finalTeam1, finalTeam2);

        int mapId = KernelUtils.getRandomMapId();

        Kernel kernel;
        if (firstUserStartsFirst) {
            kernel = new Kernel(player1, player2, fruitons, KernelUtils.makeGameSettings(mapId, gameMode), false, false);
        } else {
            kernel = new Kernel(player2, player1, fruitons, KernelUtils.makeGameSettings(mapId, gameMode), false, false);
        }

        GameData gameData = new GameData(user1, user2, player1, player2, kernel);

        try {
            gamesLock.writeLock().lock();
            userToGameData.put(user1, gameData);
            userToGameData.put(user2, gameData);
        } finally {
            gamesLock.writeLock().unlock();
        }

        sendGameReadyMessages(user1, user2, finalTeam1, finalTeam2, firstUserStartsFirst, mapId);
        userStateService.setNewState(Status.IN_BATTLE, user1, user2);
    }

    private void checkTeamValidity(final UserIdHolder user, final FruitonTeam team) {
        if (!FruitonTeamUtils.isTeamValid(user, team, userService)) {
            throw new IllegalArgumentException("Cannot create game with invalid team: " + team + " of user " + user);
        }
    }

    private FruitonTeam convertFruitonPositions(final FruitonTeam team) {
        FruitonTeam.Builder teamBuilder = FruitonTeam.newBuilder();
        teamBuilder.setName(team.getName());
        teamBuilder.addAllFruitonIDs(team.getFruitonIDsList());
        for (GameProtos.Position position : team.getPositionsList()) {
            teamBuilder.addPositions(KernelUtils.positionOf(
                    GameState.WIDTH - 1 - position.getX(),
                    GameState.HEIGHT - 1 - position.getY()
            ));
        }

        return teamBuilder.build();
    }

    private Array<Fruiton> getFruitonsArray(
            final Player player1,
            final Player player2,
            final FruitonTeam team1,
            final FruitonTeam team2
    ) {
        Array<Fruiton> fruitons = new Array<>();
        putFruitonTeamToFruitonsArray(fruitons, player1, team1);
        putFruitonTeamToFruitonsArray(fruitons, player2, team2);
        return fruitons;
    }

    private void putFruitonTeamToFruitonsArray(
            final Array<Fruiton> fruitonArray,
            final Player owner,
            final FruitonTeam team
    ) {
        if (team.getFruitonIDsCount() != team.getPositionsCount()) {
            throw new IllegalArgumentException("Every fruiton needs to have position");
        }

        for (int i = 0; i < team.getFruitonIDsCount(); i++) {
            Fruiton fruiton = KernelUtils.getFruiton(team.getFruitonIDs(i));
            fruiton.owner = owner;
            fruiton.position = KernelUtils.positionToPoint(team.getPositions(i));
            fruitonArray.push(fruiton);
        }
    }

    private void sendGameReadyMessages(
            final UserIdHolder user1,
            final UserIdHolder user2,
            final FruitonTeam team1,
            final FruitonTeam team2,
            final boolean firstUserStartsFirst,
            final int mapId
    ) {
        logger.log(Level.FINEST, "Sending game ready messages to {0} and {1}", new Object[] {user1, user2});

        sendGameReadyMessage(user1, user2, team2, firstUserStartsFirst, mapId);
        sendGameReadyMessage(user2, user1, team1, !firstUserStartsFirst, mapId);
    }

    private void sendGameReadyMessage(
            final UserIdHolder recipient,
            final UserIdHolder opponent,
            final FruitonTeam opponentTeam,
            final boolean startsFirst,
            final int mapId
    ) {
        GameProtos.GameReady gameReadyMessage = GameProtos.GameReady.newBuilder()
                .setOpponent(getPlayerInfo(opponent))
                .setOpponentTeam(opponentTeam)
                .setStartsFirst(startsFirst)
                .setMapId(mapId)
                .build();

        communicationService.send(recipient, WrapperMessage.newBuilder()
                .setGameReady(gameReadyMessage)
                .build());
    }

    private GameProtos.PlayerInfo getPlayerInfo(final UserIdHolder player) {
        return userService.getProtobufPlayerInfo(player);
    }

    @ProtobufMessage(messageCase = MessageCase.PLAYERREADY)
    private void setPlayerReady(final UserIdHolder user) {
        logger.log(Level.FINEST, "Setting user {0} ready", user);

        GameData gameData = getGameData(user);

        if (gameData == null) {
            throw new IllegalStateException("User " + user + " has no game associated");
        }

        synchronized (gameData.lock) {
            gameData.setPlayerReady(user);

            if (gameData.arePlayersReady()) {
                startGame(gameData);
            }
        }
    }

    private GameData getGameData(final UserIdHolder user) {
        try {
            gamesLock.readLock().lock();
            return userToGameData.get(user);
        } finally {
            gamesLock.readLock().unlock();
        }
    }

    private void startGame(final GameData gameData) {
        sendGameStartsMessage(gameData.player1.user);
        sendGameStartsMessage(gameData.player2.user);

        gameData.kernel.startGame();
    }

    private void sendGameStartsMessage(final UserIdHolder to) {
        communicationService.send(to, WrapperMessage.newBuilder()
                .setGameStarts(GameProtos.GameStarts.newBuilder().build())
                .build());
    }

    @ProtobufMessage(messageCase = MessageCase.ACTION)
    private void performAction(final UserIdHolder user, final GameProtos.Action protobufAction) {
        GameData gameData = getGameData(user);
        if (gameData == null) {
            throw new IllegalStateException("User " + user + " has no game associated");
        }

        synchronized (gameData.lock) {
            if (gameData.isGameOver()) {
                return;
            }

            logger.log(Level.FINEST, "User {0} is performing action {1} in game {2}",
                    new Object[] {user, protobufAction, gameData});

            Kernel kernel = gameData.kernel;

            if (!gameData.getActivePlayer().user.equals(user)) {
                logger.log(Level.WARNING,
                        "User {0} is trying to perform action even though he is no longer active, sending correct game state",
                        user);
                sendKernelState(user, kernel);
                return;
            }

            Action kernelAction = KernelUtils.getActionFromProtobuf(protobufAction, kernel);

            try {
                Array<Event> events = kernel.performAction(kernelAction);
                processEvents(events, gameData);

                onAfterAction(gameData, user, protobufAction);
            } catch (HaxeException e) {
                logger.log(Level.WARNING, "Exception while performing kernel action, sending correct game state", e);
                sendKernelState(user, kernel);
            }
        }
    }

    private void processEvents(final Array<Event> events, final GameData gameData) {
        for (int i = 0; i < events.length; i++) {
            Event e = events.__get(i);
            processEvent(e, gameData);
        }
    }

    private void processEvent(final Event e, final GameData gameData) {
        if (e instanceof GameOverEvent) {
            GameOverEvent gameOverEvent = (GameOverEvent) e;
            switch (gameOverEvent.losers.length) {
                case 1:
                    standardGameOverWithOneLoser(gameOverEvent, gameData);
                    break;
                case 2:
                    standardGameOverWithMultipleLosers(gameOverEvent, gameData);
                    break;
                default:
                    throw new IllegalStateException("GameOverEvent with undefined number of losers " + gameOverEvent);
            }
            userStateService.setNewState(Status.MAIN_MENU, gameData.player1.user, gameData.player2.user);
        } else if (e instanceof DeathEvent) {
            DeathEvent deathEvent = (DeathEvent) e;

            PlayerRecord playerKillingFruiton = gameData.getOpponentPlayer(deathEvent.fruiton.owner);
            for (Achievement achievement : achievementHelper.getKillFruitonAchievements()) {
                achievementService.updateAchievementProgress(playerKillingFruiton.user, achievement, 1);
            }
        }
    }

    private void standardGameOverWithOneLoser(final GameOverEvent event, final GameData gameData) {
        int loserId = (Integer) event.losers.__get(0);
        PlayerRecord loser = gameData.getPlayer(loserId);
        PlayerRecord winner = gameData.getOpponentPlayer(loser.player);

        standardGameOverWithOneLoser(winner.user, loser.user, gameData);
    }

    private void standardGameOverWithOneLoser(final UserIdHolder winner, final UserIdHolder loser, final GameData gameData) {
        sendGameOverMessage(winner, Reason.STANDARD, generateWinnerGameRewards(winner, gameData), winner.getName());
        sendGameOverMessage(loser, Reason.STANDARD, generateLoserGameRewards(), winner.getName());
        ratingService.adjustRating(winner, loser, GameResult.WIN);
        gameData.setGameOver();
    }

    private void standardGameOverWithMultipleLosers(final GameOverEvent event, final GameData gameData) {
        for (int i = 0; i < event.losers.length; i++) {
            int loserId = (Integer) event.losers.__get(i);
            PlayerRecord loser = gameData.getPlayer(loserId);
            sendGameOverMessage(loser.user, Reason.STANDARD, generateLoserGameRewards(), null);
            gameData.setGameOver();
        }
        ratingService.adjustRating(gameData.player1.user, gameData.player2.user, GameResult.DRAW);
    }

    private void onAfterAction(final GameData gameData, final UserIdHolder user, final GameProtos.Action protobufAction) {
        communicationService.send(gameData.getOpponentUser(user), wrapProtobufAction(protobufAction));

        incrementAchievementProgressAfterAction(user, protobufAction);
    }

    private void incrementAchievementProgressAfterAction(final UserIdHolder user, final GameProtos.Action protobufAction) {
        if (protobufAction.getId() == MoveAction.ID) {
            for (Achievement achievement : achievementHelper.getMoveActionAchievements()) {
                achievementService.updateAchievementProgress(user, achievement, 1);
            }
        }
    }

    private void sendKernelState(final UserIdHolder user, final Kernel kernel) {
        communicationService.send(user, WrapperMessage.newBuilder()
                .setStateCorrection(GameProtos.StateCorrection.newBuilder()
                        .setGameState(kernel.currentState.serializeToString()))
                .build());
    }

    @ProtobufMessage(messageCase = MessageCase.SURRENDER)
    private void playerSurrendered(final UserIdHolder surrenderedUser) {
        GameData gameData = getGameData(surrenderedUser);
        if (gameData != null) {
            synchronized (gameData.lock) {
                if (gameData.isGameOver()) {
                    return;
                }
                UserIdHolder opponent = gameData.getOpponentUser(surrenderedUser);
                gameData.setGameOver();
                sendGameOverMessage(opponent, Reason.SURRENDER, generateWinnerGameRewards(opponent, gameData),
                        opponent.getName());
                userStateService.setNewState(Status.MAIN_MENU, surrenderedUser, opponent);

                ratingService.adjustRating(surrenderedUser, opponent, GameResult.LOSE);
            }
        }
    }

    private GameRewards generateLoserGameRewards() {
        return GameRewards.newBuilder().build();
    }

    private GameRewards generateWinnerGameRewards(final UserIdHolder user, final GameData gameData) {
        List<Integer> unlockedFruitons = fruitonService.getRandomFruitons();
        for (int fruiton : unlockedFruitons) {
            userService.unlockFruiton(user, fruiton);
        }

        List<Quest> completedQuests = processWinnerCompletedQuests(user, gameData);

        userService.adjustMoney(user, STANDARD_MONEY_REWARD);

        for (Achievement achievement: achievementHelper.getWinGameAchievements()) {
            achievementService.updateAchievementProgress(user, achievement, 1);
        }

        return GameRewards.newBuilder()
                .setMoney(STANDARD_MONEY_REWARD)
                .addAllUnlockedFruitons(unlockedFruitons)
                .addAllQuests(completedQuests)
                .build();
    }

    private List<Quest> processWinnerCompletedQuests(final UserIdHolder user, final GameData gameData) {
        List<Quest> quests = questService.getAllQuests(user);
        for (Quest q : quests) {
            switch (q.getName()) {
                case WINNER_ACHIEVEMENT: {
                    questService.completeQuest(user, WINNER_ACHIEVEMENT);
                    return Collections.singletonList(q);
                }
                case FIGHTER_ACHIEVEMENT: {
                    PlayerRecord player = gameData.getPlayer(user);
                    long aliveFruitons = HaxeUtils.toStream(gameData.kernel.currentState.fruitons)
                            .filter(f -> f.owner.equals(player.player)).count();
                    if (aliveFruitons >= q.getGoal()) {
                        questService.completeQuest(user, FIGHTER_ACHIEVEMENT);
                        return Collections.singletonList(q);
                    }
                }
                case TENACIOUS_ACHIEVEMENT: {
                    PlayerRecord player = gameData.getPlayer(user);
                    Optional<Fruiton> king = HaxeUtils.toStream(gameData.kernel.currentState.fruitons)
                            .filter(f -> f.type == Fruiton.KING_TYPE)
                            .filter(f -> f.owner.equals(player.player))
                            .findAny();
                    if (king.isPresent()) {
                        if (king.get().currentAttributes.hp == king.get().originalAttributes.hp) {
                            questService.completeQuest(user, TENACIOUS_ACHIEVEMENT);
                            return Collections.singletonList(q);
                        }
                    }
                    break;
                }
                default:
                    // ignore
            }
        }
        return Collections.emptyList();
    }

    private void sendGameOverMessage(
            final UserIdHolder to,
            final Reason reason,
            final GameRewards rewards,
            final String winner
    ) {
        GameOver.Builder gameOverMessageBuilder = GameOver.newBuilder()
                .setReason(reason)
                .setGameRewards(rewards);

        if (winner != null) {
            gameOverMessageBuilder.setWinnerLogin(winner);
        }

        communicationService.send(to, WrapperMessage.newBuilder().setGameOver(gameOverMessageBuilder).build());
    }

    @Scheduled(fixedDelay = TURN_TIME_CHECK_REFRESH_TIME)
    private void checkTurnTimeLimitAndRemoveFinishedGames() {
        try {
            gamesLock.writeLock().lock();

            Instant now = Instant.now();

            HashSet<GameData> processed = new HashSet<>();
            for (Iterator<Map.Entry<UserIdHolder, GameData>> it = userToGameData.entrySet().iterator(); it.hasNext();) {
                Map.Entry<UserIdHolder, GameData> entry = it.next();

                GameData game = entry.getValue();
                synchronized (game.lock) {
                    if (game.isGameOver()) {
                        it.remove();
                        continue;
                    }

                    if (processed.contains(game)) { // map contains 2 same games (1 for each user)
                        continue;
                    }

                    if (!game.arePlayersReady()) {
                        if (game.gameCreationTime.plusSeconds(PLAYER_READY_WAIT_TIME_SEC).isBefore(now)) {
                            resolvePlayerReadyTimeLimitExceeded(game);
                        }
                        continue; // do not check for timeout in a game in which any player was not ready
                    }

                    if (game.kernel.currentState.turnState.isTimeout()) {
                        PlayerRecord activePlayer = game.getActivePlayer();

                        logger.log(Level.FINEST, "User {0} timed out, performing end turn", activePlayer.user);

                        sendTimeOutMessage(activePlayer.user);

                        GameProtos.Action endTurnAction = GameProtos.Action.newBuilder().setId(EndTurnAction.ID).build();

                        performAction(activePlayer.user, endTurnAction);
                    }

                    processed.add(game);
                }
            }
        } finally {
            gamesLock.writeLock().unlock();
        }
    }

    private void resolvePlayerReadyTimeLimitExceeded(final GameData gameData) {
        if (gameData.player1.playerReady) {
            standardGameOverWithOneLoser(gameData.player1.user, gameData.player2.user, gameData);
        } else if (gameData.player2.playerReady) {
            standardGameOverWithOneLoser(gameData.player2.user, gameData.player1.user, gameData);
        } else { // neither of the users was ready
            sendGameOverMessage(gameData.player1.user, Reason.STANDARD, generateLoserGameRewards(), null);
            sendGameOverMessage(gameData.player2.user, Reason.STANDARD, generateLoserGameRewards(), null);

            // we do not adjust ratings since none of the players responded to the game
            gameData.setGameOver();
        }
    }

    private void sendTimeOutMessage(final UserIdHolder user) {
        communicationService.send(user, WrapperMessage.newBuilder()
                .setTimeout(GameProtos.Timeout.newBuilder()).build());
    }

    private WrapperMessage wrapProtobufAction(final GameProtos.Action protobufAction) {
        return WrapperMessage.newBuilder().setAction(protobufAction).build();
    }

    @Override
    public void onUserStateChanged(final UserIdHolder user, final Status newState) {
        if (newState == Status.OFFLINE) {
            GameData gameData = getGameData(user);
            if (gameData != null) {
                synchronized (gameData.lock) {
                    if (gameData.isGameOver()) {
                        return;
                    }

                    logger.log(Level.FINER, "User {0} disconnected while playing game", user);

                    UserIdHolder opponent = gameData.getOpponentUser(user);
                    gameData.setGameOver();
                    sendGameOverMessage(opponent, Reason.DISCONNECT, generateWinnerGameRewards(opponent, gameData),
                            opponent.getName());
                    userStateService.setNewState(Status.MAIN_MENU, opponent);

                    ratingService.adjustRating(user, opponent, GameResult.LOSE);
                }
            }
        }
    }

    private static final class GameData {

        private final Instant gameCreationTime = Instant.now();

        private PlayerRecord player1;
        private PlayerRecord player2;

        private final Kernel kernel;

        private final Object lock = new Object();

        private boolean gameOver = false;

        private GameData(
                final UserIdHolder user1,
                final UserIdHolder user2,
                final Player player1,
                final Player player2,
                final Kernel kernel
        ) {
            this.player1 = new PlayerRecord(user1, player1);
            this.player2 = new PlayerRecord(user2, player2);
            this.kernel = kernel;
        }

        private boolean isGameOver() {
            return gameOver;
        }

        private void setGameOver() {
            this.gameOver = true;
        }

        private UserIdHolder getOpponentUser(final UserIdHolder user) {
            if (user.equals(player1.user)) {
                return player2.user;
            } else if (user.equals(player2.user)) {
                return player1.user;
            } else {
                throw new IllegalStateException("This game is not for user " + user);
            }
        }

        private void setPlayerReady(final UserIdHolder user) {
            if (user.equals(player1.user)) {
                player1.playerReady = true;
            } else if (user.equals(player2.user)) {
                player2.playerReady = true;
            } else {
                throw new IllegalStateException("This game is not for user " + user);
            }
        }

        private boolean arePlayersReady() {
            return player1.playerReady && player2.playerReady;
        }

        private PlayerRecord getActivePlayer() {
            int activePlayerId = kernel.currentState.get_activePlayer().id;
            return getPlayer(activePlayerId);
        }

        private PlayerRecord getInactivePlayer() {
            int inactivePlayerId = kernel.currentState.get_otherPlayer().id;
            return getPlayer(inactivePlayerId);
        }

        private PlayerRecord getPlayer(final int id) {
            if (id == player1.player.id) {
                return player1;
            } else if (id == player2.player.id) {
                return player2;
            } else {
                throw new IllegalStateException("No player in game with id " + id);
            }
        }

        private PlayerRecord getPlayer(final UserIdHolder user) {
            if (player1.user.equals(user)) {
                return player1;
            } else if (player2.user.equals(user)) {
                return player2;
            } else {
                throw new IllegalStateException(user + " is not part of this game");
            }
        }

        private PlayerRecord getOpponentPlayer(final Player player) {
            if (player1.player.equals(player)) {
                return player2;
            } else if (player2.player.equals(player)) {
                return player1;
            } else {
                throw new IllegalStateException(player + " is not a player of this game");
            }
        }

    }

    private static class PlayerRecord {

        private UserIdHolder user;
        private boolean playerReady;
        private Player player;

        PlayerRecord(final UserIdHolder user, final Player player) {
            this.user = user;
            this.player = player;
        }
    }

}
