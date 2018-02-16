package cz.cuni.mff.fruiton.service.game.impl;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.service.communication.SessionService;
import cz.cuni.mff.fruiton.service.game.PlayerService;
import cz.cuni.mff.fruiton.service.social.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public final class PlayerServiceImpl implements PlayerService {

    private final SessionService sessionService;
    private final UserService userService;

    @Autowired
    public PlayerServiceImpl(
            final SessionService sessionService,
            final UserService userService
    ) {
        this.sessionService = sessionService;
        this.userService = userService;
    }

    @Override
    public boolean isOnline(final UserIdHolder user) {
        return sessionService.getSession(user) != null;
    }

    @Override
    public boolean isOnline(final String login) {
        UserIdHolder player = userService.findUserByLogin(login);
        return isOnline(player);
    }

    @Override
    public Optional<String> getBase64Avatar(final String login) {
        UserIdHolder player = userService.findUserByLogin(login);
        return userService.getBase64Avatar(player);
    }

}
