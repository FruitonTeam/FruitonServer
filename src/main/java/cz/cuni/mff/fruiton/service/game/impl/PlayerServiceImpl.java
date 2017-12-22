package cz.cuni.mff.fruiton.service.game.impl;

import cz.cuni.mff.fruiton.dao.domain.FruitonTeam;
import cz.cuni.mff.fruiton.dao.domain.FruitonTeamMember;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.service.communication.SessionService;
import cz.cuni.mff.fruiton.service.game.PlayerService;
import cz.cuni.mff.fruiton.util.KernelUtils;
import fruiton.kernel.Fruiton;
import org.apache.commons.collections4.ListUtils;
import cz.cuni.mff.fruiton.service.util.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@PropertySource("classpath:game.properties")
public final class PlayerServiceImpl implements PlayerService {

    @Value("#{'${default.unlocked.fruitons}'.split(',')}")
    private List<Integer> defaultUnlockedFruitons;

    private final SessionService sessionService;
    private final UserRepository userRepository;
    private final ImageService imageService;

    @Autowired
    public PlayerServiceImpl(
            final SessionService sessionService,
            final UserRepository userRepository,
            final ImageService imageService
    ) {
        this.sessionService = sessionService;
        this.userRepository = userRepository;
        this.imageService = imageService;
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

    @Override
    public Optional<String> getBase64Avatar(final String login) throws IOException {
        User player = userRepository.findByLogin(login);
        if (player.isAvatarSet()) {
            return Optional.of(imageService.getBase64Avatar(userRepository.findByLogin(login)));
        }

        return Optional.empty();
    }

    @Override
    public void addTeam(final User user, final FruitonTeam teamToAdd) {
        List<Integer> availableFruitons = getAvailableFruitons(user);

        for (FruitonTeamMember member : teamToAdd.getFruitons()) {
            if (!availableFruitons.contains(member.getFruitonId())) {
                throw new IllegalArgumentException("User does not have unlocked fruiton with id " + member.getFruitonId());
            }
        }

        // if team with the same name exists then remove it
        user.getTeams().removeIf(ft -> ft.getName().equals(teamToAdd.getName()));
        user.getTeams().add(teamToAdd);
        userRepository.save(user);
    }

    @Override
    public void removeTeam(final User user, final String teamToRemove) {
        user.getTeams().removeIf(ft -> ft.getName().equals(teamToRemove));
        userRepository.save(user);
    }

    @Override
    public List<Integer> getAvailableFruitons(final User user) {
        if (user == null) {
            throw new IllegalArgumentException("Cannot get available fruitons for null user");
        }
        return ListUtils.union(defaultUnlockedFruitons, user.getUnlockedFruitons());
    }

    @Override
    public List<Fruiton> getFruitonsAvailableForSelling(final User user) {
        List<Integer> fruitonsForSell = user.getUnlockedFruitons();
        fruitonsForSell.removeAll(defaultUnlockedFruitons);

        return fruitonsForSell.stream().distinct().map(KernelUtils::getFruiton).collect(Collectors.toList());
    }

}
