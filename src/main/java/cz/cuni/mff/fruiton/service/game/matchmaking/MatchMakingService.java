package cz.cuni.mff.fruiton.service.game.matchmaking;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.service.util.UserStateService.OnUserStateChangedListener;

public interface MatchMakingService extends OnUserStateChangedListener {

    void findGame(UserIdHolder user, GameProtos.FindGame findGameMsg);

    void removeFromMatchMaking(UserIdHolder user);

}
