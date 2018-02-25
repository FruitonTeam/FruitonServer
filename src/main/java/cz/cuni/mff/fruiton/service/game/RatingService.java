package cz.cuni.mff.fruiton.service.game;

import cz.cuni.mff.fruiton.dao.UserIdHolder;

public interface RatingService {

    void adjustRating(UserIdHolder player1, UserIdHolder player2, GameResult firstPlayerResult);

}
