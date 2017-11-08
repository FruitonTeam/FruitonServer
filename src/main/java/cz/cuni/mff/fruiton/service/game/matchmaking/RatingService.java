package cz.cuni.mff.fruiton.service.game.matchmaking;

import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.service.game.GameResult;

public interface RatingService {

    void adjustRating(User player1, User player2, GameResult firstPlayerResult);

}
