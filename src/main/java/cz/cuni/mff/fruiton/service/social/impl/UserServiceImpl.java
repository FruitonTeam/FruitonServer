package cz.cuni.mff.fruiton.service.social.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.service.game.QuestService;
import cz.cuni.mff.fruiton.service.social.EmailConfirmationService;
import cz.cuni.mff.fruiton.service.social.UserService;
import cz.cuni.mff.fruiton.service.util.ImageService;
import org.kohsuke.randname.RandomNameGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public final class UserServiceImpl implements UserService {

    @FunctionalInterface
    private interface AvatarSaver<T> {

        String save(T t) throws IOException;

    }

    private static final int NAME_GENERATION_RETRY_COUNT = 5;

    private static final int RANDOM_GOOGLE_SUFFIX_SIZE = 3;

    private static final Logger logger = Logger.getLogger(UserServiceImpl.class.getName());

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

    @Override
    public void changeAvatar(final User user, final MultipartFile avatar) {
        changeAvatar(user, avatar, imageService::saveAvatar);
    }

    @Override
    public void changeAvatar(final User user, final String url) {
        changeAvatar(user, url, imageService::saveAvatar);
    }

    private <T> void changeAvatar(final User user, final T avatar, final AvatarSaver<T> avatarSaver) {
        if (user == null) {
            throw new IllegalArgumentException("Cannot change avatar for null user");
        }

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
    public void changePassword(final User user, final String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        repository.save(user);
    }

    @Override
    public void changeEmail(final User user, final String newEmail) {
        user.setEmail(newEmail);
        user.setEmailConfirmed(false);
        repository.save(user);

        emailConfirmationService.sendEmailConfirmationRequest(user);
    }

    @Override
    public User findUserByLogin(final String login) {
        if (login == null) {
            throw new IllegalArgumentException("Cannot find user for null login");
        }
        User user = repository.findByLogin(login);

        if (user == null) {
            throw new UsernameNotFoundException("Cannot find user for: " + login);
        }
        return user;
    }

    @Override
    public User findUser(final String id) {
        if (id == null) {
            throw new IllegalArgumentException("Cannot find user for null id");
        }

        User user = repository.findOne(id);

        if (user == null) {
            throw new UsernameNotFoundException("Cannot find user for: " + id);
        }
        return user;
    }

    @Override
    public GameProtos.LoggedPlayerInfo getLoggedPlayerInfo(final User user) {
        if (user == null) {
            throw new IllegalArgumentException("Cannot get logged player's info for null user");
        }
        GameProtos.LoggedPlayerInfo.Builder builder =  GameProtos.LoggedPlayerInfo.newBuilder()
                .setLogin(user.getLogin())
                .setRating(user.getRating())
                .setMoney(user.getMoney())
                .addAllQuests(questService.getAllQuests(user));

        if (user.isAvatarSet()) {
            try {
                builder.setAvatar(imageService.getBase64Avatar(user));
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

}
