package cz.cuni.mff.fruiton.service.game;

import cz.cuni.mff.fruiton.dao.domain.User;

public interface PlayerService {

    boolean isOnline(User user);

    boolean isOnline(String login);

}
