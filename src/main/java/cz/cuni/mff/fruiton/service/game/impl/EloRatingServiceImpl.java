package cz.cuni.mff.fruiton.service.game.impl;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.service.game.GameResult;
import cz.cuni.mff.fruiton.service.game.RatingService;
import cz.cuni.mff.fruiton.service.social.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public final class EloRatingServiceImpl implements RatingService {

    public static final int DEFAULT_RATING = 1500;

    private static final int K_FACTOR = 32;

    private static final Logger logger = Logger.getLogger(EloRatingServiceImpl.class.getName());

    private final UserService userService;

    @Autowired
    public EloRatingServiceImpl(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public void adjustRating(final UserIdHolder player1, final UserIdHolder player2, final GameResult firstPlayerResult) {
        int player1Rating = userService.getRating(player1);
        int player2Rating = userService.getRating(player2);

        Pair<Double, Double> expectedScores = computeExpectedScores(player1Rating, player2Rating);

        int rating1 = (int) (player1Rating
                + K_FACTOR * (gameResultScore(firstPlayerResult) - expectedScores.getFirst()));
        int rating2 = (int) (player2Rating
                + K_FACTOR * (gameResultScore(firstPlayerResult.inverse()) - expectedScores.getSecond()));

        logger.log(Level.FINEST, "Changing rating for {0} from {1} to {2}",
                new Object[] {player1, player1Rating, rating1});
        logger.log(Level.FINEST, "Changing rating for {0} from {1} to {2}",
                new Object[] {player2, player2Rating, rating2});

        userService.setRating(player1, rating1);
        userService.setRating(player2, rating2);
    }

    private static Pair<Double, Double> computeExpectedScores(final int rating1, final int rating2) {
        final int base = 10;
        final double divisor = 400.0;

        double q1 = Math.pow(base, rating1 / divisor);
        double q2 = Math.pow(base, rating2 / divisor);

        double expectedScore1 = q1 / (q1 + q2);
        double expectedScore2 = q2 / (q1 + q2);

        return Pair.of(expectedScore1, expectedScore2);
    }

    private static double gameResultScore(final GameResult result) {
        switch (result) {
            case WIN:
                return 1;
            case LOSE:
                return 0;
            case DRAW:
                return 0.5;
            default:
                throw new IllegalArgumentException("Unknown game result " + result);
        }
    }

}
