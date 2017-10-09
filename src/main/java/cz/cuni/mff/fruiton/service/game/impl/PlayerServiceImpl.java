package cz.cuni.mff.fruiton.service.game.impl;

import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.service.communication.SessionService;
import cz.cuni.mff.fruiton.service.game.PlayerService;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@PropertySource("classpath:game.properties")
public final class PlayerServiceImpl implements PlayerService {

    @Value("#{'${default.unlocked.fruitons}'.split(',')}")
    private List<Integer> defaultUnlockedFruitons;

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

    public List<Integer> getAvailableFruitons(final String login) {
        User user = userRepository.findByLogin(login);
        if (user == null) {
            throw new IllegalArgumentException("Unknown user " + login);
        }

        return ListUtils.union(defaultUnlockedFruitons, user.getUnlockedFruitons());
    }

}
