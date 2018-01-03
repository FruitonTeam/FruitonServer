package cz.cuni.mff.fruiton.service.social.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dao.domain.Achievement;
import cz.cuni.mff.fruiton.dao.domain.FruitonTeam;
import cz.cuni.mff.fruiton.dao.domain.FruitonTeamMember;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.dto.form.EditProfileForm;
import cz.cuni.mff.fruiton.service.game.QuestService;
import cz.cuni.mff.fruiton.service.social.EmailConfirmationService;
import cz.cuni.mff.fruiton.service.social.UserService;
import cz.cuni.mff.fruiton.service.util.ImageService;
import cz.cuni.mff.fruiton.util.KernelUtils;
import fruiton.kernel.Fruiton;
import org.apache.commons.collections4.ListUtils;
import org.kohsuke.randname.RandomNameGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@PropertySource("classpath:game.properties")
public final class UserServiceImpl implements UserService {

    @FunctionalInterface
    private interface AvatarSaver<T> {

        String save(T t) throws IOException;

    }

    private static final int NAME_GENERATION_RETRY_COUNT = 5;

    private static final int RANDOM_GOOGLE_SUFFIX_SIZE = 3;

    private static final Logger logger = Logger.getLogger(UserServiceImpl.class.getName());

    @Value("#{'${default.unlocked.fruitons}'.split(',')}")
    private List<Integer> defaultUnlockedFruitons;

    private final RandomNameGenerator nameGenerator = new RandomNameGenerator();

    private final UserRepository repository;

    private final ImageService imageService;
    private final EmailConfirmationService emailConfirmationService;

    private final QuestService questService;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(
            final UserRepository repository,
            final ImageService imageService,
            final EmailConfirmationService emailConfirmationService,
            final QuestService questService,
            final PasswordEncoder passwordEncoder
    ) {
        this.repository = repository;
        this.imageService = imageService;
        this.emailConfirmationService = emailConfirmationService;
        this.questService = questService;
        this.passwordEncoder = passwordEncoder;
    }

    private User getUser(final UserIdHolder idHolder) {
        return repository.findOne(idHolder.getId());
    }

    @Override
    public void changeAvatar(final UserIdHolder user, final MultipartFile avatar) {
        changeAvatar(user, avatar, imageService::saveAvatar);
    }

    @Override
    public void changeAvatar(final UserIdHolder user, final String url) {
        changeAvatar(user, url, imageService::saveAvatar);
    }

    private <T> void changeAvatar(final UserIdHolder idHolder, final T avatar, final AvatarSaver<T> avatarSaver) {
        if (idHolder == null) {
            throw new IllegalArgumentException("Cannot change avatar for null user");
        }

        User user = getUser(idHolder);
        if (user.isAvatarSet()) {
            imageService.removeAvatar(user);
        }

        if (avatar != null) {
            try {
                String avatarImageName = avatarSaver.save(avatar);
                user.setAvatar(avatarImageName);
                repository.save(user);
            } catch (IOException e) {
                logger.log(Level.WARNING, "Cannot save avatar {0} for user {1}", new Object[] {avatar, user});
            }
        } else {
            user.setAvatar(null);
            repository.save(user);
        }
    }

    @Override
    public Optional<String> getBase64Avatar(final UserIdHolder idHolder) {
        return getBase64Avatar(getUser(idHolder));
    }

    @Override
    public Optional<String> getBase64Avatar(final String login) {
        return getBase64Avatar(repository.findByLogin(login));
    }

    private Optional<String> getBase64Avatar(final User user) {
        if (user.isAvatarSet()) {
            try {
                return Optional.of(imageService.getBase64Avatar(user.getAvatar()));
            } catch (IOException e) {
                logger.log(Level.WARNING, "Could not get base64 avatar for {0}", user);
            }
        }
        return Optional.empty();
    }

    @Override
    public void changePassword(final UserIdHolder idHolder, final String newPassword) {
        User user = getUser(idHolder);
        user.setPassword(passwordEncoder.encode(newPassword));
        repository.save(user);
    }

    @Override
    public void changeEmail(final UserIdHolder idHolder, final String newEmail) {
        User user = getUser(idHolder);
        user.setEmail(newEmail);
        user.setEmailConfirmed(false);
        repository.save(user);

        emailConfirmationService.sendEmailConfirmationRequest(user);
    }

    @Override
    public UserIdHolder findUserByLogin(final String login) {
        if (login == null) {
            throw new IllegalArgumentException("Cannot find user for null login");
        }
        User user = repository.findByLogin(login);

        if (user == null) {
            throw new UsernameNotFoundException("Cannot find user for: " + login);
        }
        return UserIdHolder.of(user);
    }

    @Override
    public UserIdHolder findUser(final String id) {
        if (id == null) {
            throw new IllegalArgumentException("Cannot find user for null id");
        }

        User user = repository.findOne(id);

        if (user == null) {
            throw new UsernameNotFoundException("Cannot find user for: " + id);
        }
        return UserIdHolder.of(user);
    }

    @Override
    public GameProtos.LoggedPlayerInfo getLoggedPlayerInfo(final UserIdHolder idHolder) {
        if (idHolder == null) {
            throw new IllegalArgumentException("Cannot get logged player's info for null user");
        }

        User user = getUser(idHolder);

        GameProtos.LoggedPlayerInfo.Builder builder =  GameProtos.LoggedPlayerInfo.newBuilder()
                .setLogin(user.getLogin())
                .setRating(user.getRating())
                .setMoney(user.getMoney())
                .addAllQuests(questService.getAllQuests(idHolder));

        if (user.isAvatarSet()) {
            try {
                builder.setAvatar(imageService.getBase64Avatar(user.getAvatar()));
            } catch (IOException e) {
                logger.log(Level.WARNING, "Could not get base64 encoded image for {0}", user);
            }
        }

        return builder.build();
    }

    @Override
    public String generateRandomName(final GoogleIdToken.Payload payload) {
        int tries = 0;

        String name = nameGenerator.next();
        while (tries < NAME_GENERATION_RETRY_COUNT && repository.existsByLogin(name)) {
            name = nameGenerator.next();
            tries++;
        }

        if (tries >= NAME_GENERATION_RETRY_COUNT) {
            // get google first name and remove all non-alpha numeric characters
            name = ((String) payload.get("given_name")).replaceAll("[^a-zA-Z0-9]", "");
            while (repository.existsByLogin(name)) {
                name += StringUtils.randomAlphanumeric(RANDOM_GOOGLE_SUFFIX_SIZE);
            }
        }

        return name;
    }

    @Override
    public void removeFruitonFromUnlockedFruitons(final UserIdHolder idHolder, final int fruitonId) {
        if (idHolder == null) {
            throw new IllegalArgumentException("Cannot remove unlocked fruiton for null user");
        }

        User user = getUser(idHolder);
        user.removeFruitonFromUnlockedFruitons(fruitonId);
        repository.save(user);
    }

    @Override
    public void unlockFruiton(final UserIdHolder idHolder, final int fruitonId) {
        if (idHolder == null) {
            throw new IllegalArgumentException("Cannot unlock fruiton for null user");
        }

        User user = getUser(idHolder);
        user.unlockFruiton(fruitonId);
        repository.save(user);
    }

    @Override
    public void adjustMoney(final UserIdHolder idHolder, final int change) {
        if (idHolder == null) {
            throw new IllegalArgumentException("Cannot adjust money for null user");
        }

        User user = getUser(idHolder);
        user.adjustMoney(change);
        repository.save(user);
    }

    @Override
    public void addFruitonTeam(final UserIdHolder idHolder, final FruitonTeam teamToAdd) {
        User user = getUser(idHolder);

        List<Integer> availableFruitons = getAvailableFruitons(user);

        for (FruitonTeamMember member : teamToAdd.getFruitons()) {
            if (!availableFruitons.contains(member.getFruitonId())) {
                throw new IllegalArgumentException("User does not have unlocked fruiton with id " + member.getFruitonId());
            }
        }

        // if team with the same name exists then remove it
        user.getTeams().removeIf(ft -> ft.getName().equals(teamToAdd.getName()));
        user.getTeams().add(teamToAdd);
        repository.save(user);
    }

    @Override
    public void removeTeam(final UserIdHolder idHolder, final String teamToRemove) {
        User user = getUser(idHolder);
        user.getTeams().removeIf(ft -> ft.getName().equals(teamToRemove));
        repository.save(user);
    }

    @Override
    public List<Integer> getAvailableFruitons(final UserIdHolder idHolder) {
        if (idHolder == null) {
            throw new IllegalArgumentException("Cannot get available fruitons for null user");
        }
        return getAvailableFruitons(getUser(idHolder));
    }

    private List<Integer> getAvailableFruitons(final User user) {
        return ListUtils.union(defaultUnlockedFruitons, user.getUnlockedFruitons());
    }

    @Override
    public List<Fruiton> getFruitonsAvailableForSelling(final UserIdHolder idHolder) {
        User user = getUser(idHolder);
        List<Integer> fruitonsForSell = user.getUnlockedFruitons();
        fruitonsForSell.removeAll(defaultUnlockedFruitons);

        return fruitonsForSell.stream().distinct().map(KernelUtils::getFruiton).collect(Collectors.toList());
    }

    @Override
    public List<FruitonTeam> getFruitonTeams(final UserIdHolder idHolder) {
        return getUser(idHolder).getTeams();
    }

    @Override
    public void unlockAchievement(final UserIdHolder idHolder, final Achievement achievement) {
        User user = getUser(idHolder);

        user.getUnlockedAchievements().add(achievement);
        repository.save(user);
    }

    @Override
    public List<Achievement> getUnlockedAchievements(final UserIdHolder idHolder) {
        return getUser(idHolder).getUnlockedAchievements();
    }

    @Override
    public PlayerInfo getPlayerInfo(final String login) {
        User user = repository.findByLogin(login);
        if (user == null) {
            throw new UsernameNotFoundException("No user with login " + login);
        }

        return new PlayerInfo(user.getLogin(), user.getRating(), user.getAvatarWebImageMapping());
    }

    @Override
    public GameProtos.PlayerInfo getProtobufPlayerInfo(final UserIdHolder idHolder) {
        User player = getUser(idHolder);

        GameProtos.PlayerInfo.Builder builder = GameProtos.PlayerInfo.newBuilder()
                .setLogin(player.getLogin())
                .setRating(player.getRating());

        if (player.isAvatarSet()) {
            try {
                builder.setAvatar(imageService.getBase64Avatar(player.getAvatar()));
            } catch (IOException e) {
                logger.log(Level.WARNING, "Could not encode avatar for user {0}", player);
            }
        }

        return builder.build();
    }

    @Override
    public int getRating(final UserIdHolder idHolder) {
        return getUser(idHolder).getRating();
    }

    @Override
    public boolean isAvatarSet(final UserIdHolder idHolder) {
        return getUser(idHolder).isAvatarSet();
    }

    @Override
    public EditProfileForm getEditProfileForm(final UserIdHolder idHolder) {
        return EditProfileForm.of(getUser(idHolder).getEmail());
    }

}
