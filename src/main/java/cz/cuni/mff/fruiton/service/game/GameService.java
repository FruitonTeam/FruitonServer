package cz.cuni.mff.fruiton.service.game;

import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dto.GameProtos;

public interface GameService {

    void createGame(User user1, GameProtos.FruitonTeam team1, User user2, GameProtos.FruitonTeam team2);

    void setPlayerReady(User user);

    void performAction(User user, GameProtos.Action action);

    void userDisconnected(User user);

}
