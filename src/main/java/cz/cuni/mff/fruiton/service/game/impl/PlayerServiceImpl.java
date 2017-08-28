package cz.cuni.mff.fruiton.service.game.impl;

import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.service.communication.SessionService;
import cz.cuni.mff.fruiton.service.game.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public final class PlayerServiceImpl implements PlayerService {

    private final SessionService sessionService;
    private final UserRepository userRepository;

    @Autowired
    public PlayerServiceImpl(final SessionService sessionService, final UserRepository userRepository) {
        this.sessionService = sessionService;
        this.userRepository = userRepository;
    }

    @Override
    public boolean isOnline(final User user) {
        return sessionService.getSession(user) != null;
    }

    @Override
    public boolean isOnline(final String login) {
        User player = userRepository.findByLogin(login);
        if (player == null) {
            throw new UsernameNotFoundException("No user with login " + login);
        }
        return isOnline(player);
    }

}
