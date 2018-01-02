package cz.cuni.mff.fruiton.service.game;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.GameProtos;

public interface GameService {

    void createGame(UserIdHolder user1, GameProtos.FruitonTeam team1, UserIdHolder user2, GameProtos.FruitonTeam team2);

    void setPlayerReady(UserIdHolder user);

    void performAction(UserIdHolder user, GameProtos.Action action);

    void userDisconnected(UserIdHolder user);

    void playerSurrendered(UserIdHolder surrenderedUser);

}
