package cz.cuni.mff.fruiton.service.game;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.GameProtos.FruitonTeam;
import cz.cuni.mff.fruiton.dto.GameProtos.GameMode;
import cz.cuni.mff.fruiton.service.util.UserStateService.OnUserStateChangedListener;

public interface GameService extends OnUserStateChangedListener {

    /**
     * Creates game.
     * @param player1 first player
     * @param team1 team of the first player
     * @param player2 second player
     * @param team2 team of the second player
     * @param gameMode mode of the newly created game
     */
    void createGame(
            UserIdHolder player1,
            FruitonTeam team1,
            UserIdHolder player2,
            FruitonTeam team2,
            GameMode gameMode
    );

}
