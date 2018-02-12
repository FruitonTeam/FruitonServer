package cz.cuni.mff.fruiton.service.game.matchmaking;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.GameProtos;

public interface ChallengeService {

    void challenge(UserIdHolder from, GameProtos.Challenge challengeMsg);

}
