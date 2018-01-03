package cz.cuni.mff.fruiton.service.game.matchmaking;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.GameProtos;

public interface MatchMakingService {

    void findGame(UserIdHolder user, GameProtos.FindGame findGameMsg);

    void removeFromMatchMaking(UserIdHolder user);

}
