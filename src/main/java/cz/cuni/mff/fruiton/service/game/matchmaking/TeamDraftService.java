package cz.cuni.mff.fruiton.service.game.matchmaking;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.GameProtos.GameMode;

public interface TeamDraftService {

    /**
     * Starts draft between specified players.
     * @param player1 first player
     * @param player2 second player
     * @param gameMode mode of the game the players are going to play after draft is completed
     */
    void startDraft(UserIdHolder player1, UserIdHolder player2, GameMode gameMode);

}
