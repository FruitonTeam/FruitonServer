package cz.cuni.mff.fruiton.service.game;

import cz.cuni.mff.fruiton.component.util.OnDisconnectedListener;
import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.GameProtos;

public interface GameService extends OnDisconnectedListener {

    class GameException extends RuntimeException {

        public GameException(final String message) {
            super(message);
        }
    }

    void createGame(
            UserIdHolder user1,
            GameProtos.FruitonTeam team1,
            UserIdHolder user2,
            GameProtos.FruitonTeam team2,
            GameProtos.FindGame.GameMode gameMode
    );

    void setPlayerReady(UserIdHolder user);

    void performAction(UserIdHolder user, GameProtos.Action action);

    void playerSurrendered(UserIdHolder surrenderedUser);

}
