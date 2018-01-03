package cz.cuni.mff.fruiton.service.game;

import cz.cuni.mff.fruiton.dao.UserIdHolder;

import java.io.IOException;
import java.util.Optional;

public interface PlayerService {

    boolean isOnline(UserIdHolder user);

    boolean isOnline(String login);

    Optional<String> getBase64Avatar(String login) throws IOException;

}
