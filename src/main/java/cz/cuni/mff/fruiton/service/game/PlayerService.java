package cz.cuni.mff.fruiton.service.game;

import cz.cuni.mff.fruiton.dao.domain.User;

import java.util.List;

public interface PlayerService {

    boolean isOnline(User user);

    boolean isOnline(String login);

    List<Integer> getAvailableFruitons(String login);

}
