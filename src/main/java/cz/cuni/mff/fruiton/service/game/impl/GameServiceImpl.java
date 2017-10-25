package cz.cuni.mff.fruiton.service.game.impl;

import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dto.CommonProtos;
import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.service.communication.CommunicationService;
import cz.cuni.mff.fruiton.service.game.GameService;
import cz.cuni.mff.fruiton.service.game.PlayerService;
import cz.cuni.mff.fruiton.service.util.ImageService;
import cz.cuni.mff.fruiton.util.KernelUtils;
import fruiton.kernel.Fruiton;
import fruiton.kernel.GameState;
import fruiton.kernel.Kernel;
import fruiton.kernel.Player;
import fruiton.kernel.actions.Action;
import fruiton.kernel.events.Event;
import haxe.root.Array;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public final class GameServiceImpl implements GameService {

    private static final Logger logger = Logger.getLogger(GameServiceImpl.class.getName());

    private static final AtomicInteger ATOMIC_INT = new AtomicInteger();

    private final Random random = new Random();

    private final Map<User, GameData> userToGameData = new Hashtable<>();

    private final CommunicationService communicationService;

    private final PlayerService playerService;

    private final ImageService imageService;

    @Autowired
    public GameServiceImpl(
            final CommunicationService communicationService,
            final PlayerService playerService,
            final ImageService imageService
    ) {
        this.communicationService = communicationService;
        this.playerService = playerService;
        this.imageService = imageService;
    }

    @Override
    public void createGame(
            final User user1,
            final GameProtos.FruitonTeam team1,
            final User user2,
            final GameProtos.FruitonTeam team2
    ) {
        logger.log(Level.FINE, "Creating game between {0} and {1} with teams {2} and {3}",
                new Object[] {user1, user2, team1, team2});

        user1.setState(User.State.IN_GAME);
        user2.setState(User.State.IN_GAME);

        Player player1 = new Player(ATOMIC_INT.getAndIncrement());
        Player player2 = new Player(ATOMIC_INT.getAndIncrement());

        boolean firstUserStartsFirst = true; // TODO: after testing change to random.nextBoolean();

        GameProtos.FruitonTeam finalTeam1 = team1;
        GameProtos.FruitonTeam finalTeam2 = team2;

        if (firstUserStartsFirst) {
            finalTeam2 = convertFruitonPositions(team2);
        } else {
            finalTeam1 = convertFruitonPositions(team1);
        }

        Array<Fruiton> fruitons = getFruitonsArray(player1, player2, finalTeam1, finalTeam2);

        Kernel kernel;
        if (firstUserStartsFirst) {
            kernel = new Kernel(player1, player2, fruitons);
        } else {
            kernel = new Kernel(player2, player1, fruitons);
        }

        GameData gameData = new GameData(user1, player1, user2, player2, kernel);

        userToGameData.put(user1, gameData);
        userToGameData.put(user2, gameData);

        sendGameReadyMessages(user1, user2, finalTeam1, finalTeam2, firstUserStartsFirst);
    }

    private GameProtos.FruitonTeam convertFruitonPositions(final GameProtos.FruitonTeam team) {
        GameProtos.FruitonTeam.Builder teamBuilder = GameProtos.FruitonTeam.newBuilder();
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
            final GameProtos.FruitonTeam team1,
            final GameProtos.FruitonTeam team2
    ) {
        Array<Fruiton> fruitons = new Array<>();
        putFruitonTeamToFruitonsArray(fruitons, player1, team1);
        putFruitonTeamToFruitonsArray(fruitons, player2, team2);
        return fruitons;
    }

    private void putFruitonTeamToFruitonsArray(
            final Array<Fruiton> fruitonArray,
            final Player owner,
            final GameProtos.FruitonTeam team
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
            final User user1,
            final User user2,
            final GameProtos.FruitonTeam team1,
            final GameProtos.FruitonTeam team2,
            final boolean firstUserStartsFirst
    ) {
        logger.log(Level.FINEST, "Sending game ready messages to {0} and {1}", new Object[] {user1, user2});

        sendGameReadyMessage(user1, user2, team2, firstUserStartsFirst);
        sendGameReadyMessage(user2, user1, team1, !firstUserStartsFirst);
    }

    private void sendGameReadyMessage(
            final User recipient,
            final User opponent,
            final GameProtos.FruitonTeam opponentTeam,
            final boolean startsFirst
    ) {
        GameProtos.GameReady gameReadyMessage = GameProtos.GameReady.newBuilder()
                .setOpponent(getPlayerInfo(opponent))
                .setOpponentTeam(opponentTeam)
                .setStartsFirst(startsFirst)
                .build();

        communicationService.send(recipient, CommonProtos.WrapperMessage.newBuilder()
                .setGameReady(gameReadyMessage)
                .build());
    }

    private GameProtos.PlayerInfo getPlayerInfo(final User player) {

        GameProtos.PlayerInfo.Builder builder = GameProtos.PlayerInfo.newBuilder()
                .setLogin(player.getLogin())
                .setRating(player.getRating());

        if (player.isAvatarSet()) {
            try {
                builder.setAvatar(imageService.getBase64Avatar(player));
            } catch (IOException e) {
                logger.log(Level.WARNING, "Could not encode avatar for user {0}", player);
            }
        }

        return builder.build();
    }

    @Override
    public void setPlayerReady(final User user) {
        logger.log(Level.FINEST, "Setting user {0} ready", user);

        GameData gameData = userToGameData.get(user);

        if (gameData == null) {
            throw new IllegalStateException("User " + user + " has no game associated");
        }

        synchronized (this) {
            gameData.setPlayerReady(user);

            if (gameData.arePlayersReady()) {
                startGame(gameData);
            }
        }
    }

    private void startGame(final GameData gameData) {
        sendGameStartsMessage(gameData.user1);
        sendGameStartsMessage(gameData.user2);

        gameData.kernel.startGame();
    }

    private void sendGameStartsMessage(final User to) {
        communicationService.send(to, CommonProtos.WrapperMessage.newBuilder()
                .setGameStarts(GameProtos.GameStarts.newBuilder().build())
                .build());
    }

    @Override
    public void performAction(final User user, final GameProtos.Action protobufAction) {
        GameData gameData = userToGameData.get(user);
        if (gameData == null) {
            throw new IllegalStateException("User " + user + " has no game associated");
        }

        logger.log(Level.FINEST, "User {0} is performing action {2} in game {2}",
                new Object[] {user, protobufAction, gameData});

        communicationService.send(gameData.getOpponentUser(user),
                CommonProtos.WrapperMessage.newBuilder().setAction(protobufAction).build());

        Kernel kernel = gameData.kernel;

        Action kernelAction = KernelUtils.getActionFromProtobuf(protobufAction, kernel);

        Array<Event> events = kernel.performAction(kernelAction);
        processEvents(events);
    }

    private void processEvents(final Array<Event> events) {
        for (int i = 0; i < events.length; i++) {
            Event e = events.__get(i);
            processEvent(e);
        }
    }

    private void processEvent(final Event e) {
        // TODO: specific handling
    }

    @Override
    public void userDisconnected(final User user) {
        GameData gameData = userToGameData.get(user);

        User opponent = gameData.getOpponentUser(user);

        if (playerService.isOnline(opponent)) {
            // TODO: set correct reason and results
            sendGameOverMessage(opponent, 0, GameProtos.GameResults.newBuilder().build());
        }
    }

    private void sendGameOverMessage(final User to, final int reason, final GameProtos.GameResults results) {
        communicationService.send(to, CommonProtos.WrapperMessage.newBuilder()
                .setGameOver(GameProtos.GameOver.newBuilder()
                        .setReason(reason)
                        .setResults(results)
                        .build())
                .build());
    }

    private static final class GameData {

        private final User user1;
        private final Player player1;
        private boolean player1Ready;

        private final User user2;
        private final Player player2;
        private boolean player2Ready;

        private final Kernel kernel;

        private GameData(
                final User user1,
                final Player player1,
                final User user2,
                final Player player2,
                final Kernel kernel
        ) {
            this.user1 = user1;
            this.player1 = player1;
            this.user2 = user2;
            this.player2 = player2;
            this.kernel = kernel;
        }

        private User getOpponentUser(final User user) {
            if (user.equals(user1)) {
                return user2;
            } else if (user.equals(user2)) {
                return user1;
            } else {
                throw new IllegalStateException("This game is not for user " + user);
            }
        }

        private void setPlayerReady(final User user) {
            if (user.equals(user1)) {
                player1Ready = true;
            } else if (user.equals(user2)) {
                player2Ready = true;
            } else {
                throw new IllegalStateException("This game is not for user " + user);
            }
        }

        private boolean arePlayersReady() {
            return player1Ready && player2Ready;
        }

    }

}
