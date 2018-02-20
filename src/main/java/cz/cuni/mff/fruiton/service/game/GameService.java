package cz.cuni.mff.fruiton.service.game;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.service.util.UserStateService.OnUserStateChangedListener;

public interface GameService extends OnUserStateChangedListener {

    void createGame(
            UserIdHolder user1,
            GameProtos.FruitonTeam team1,
            UserIdHolder user2,
            GameProtos.FruitonTeam team2,
            GameProtos.GameMode gameMode
    );

}
