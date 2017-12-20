package cz.cuni.mff.fruiton.service.game;

import cz.cuni.mff.fruiton.dao.domain.FruitonTeam;
import cz.cuni.mff.fruiton.dao.domain.User;

import java.util.List;

import java.io.IOException;
import java.util.Optional;

public interface PlayerService {

    boolean isOnline(User user);

    boolean isOnline(String login);

    List<Integer> getAvailableFruitons(User user);

    void addTeam(User user, FruitonTeam fruitonTeam);

    void removeTeam(User user, String teamName);

    Optional<String> getBase64Avatar(String login) throws IOException;

}
