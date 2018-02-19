package cz.cuni.mff.fruiton.service.game.matchmaking;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.service.game.GameResult;

public interface RatingService {

    void adjustRating(UserIdHolder player1, UserIdHolder player2, GameResult firstPlayerResult);

}
