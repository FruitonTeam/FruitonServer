package cz.cuni.mff.fruiton.service.game;

import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dto.GameProtos;

public interface MatchMakingService {

    void findGame(User user, GameProtos.FindGame findGameMsg);

    void removeFromMatchMaking(User user);

}
