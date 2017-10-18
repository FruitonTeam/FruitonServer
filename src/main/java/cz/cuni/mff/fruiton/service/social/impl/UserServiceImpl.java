package cz.cuni.mff.fruiton.service.social.impl;

import cz.cuni.mff.fruiton.dao.domain.FruitonTeam;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.service.social.EmailConfirmationService;
import cz.cuni.mff.fruiton.service.social.UserService;
import cz.cuni.mff.fruiton.service.util.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = Logger.getLogger(UserServiceImpl.class.getName());

    private final UserRepository repository;

    private final ImageService imageService;
    private final EmailConfirmationService emailConfirmationService;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(
            final UserRepository repository,
            final ImageService imageService,
            final EmailConfirmationService emailConfirmationService,
            final PasswordEncoder passwordEncoder
    ) {
        this.repository = repository;
        this.imageService = imageService;
        this.emailConfirmationService = emailConfirmationService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public final void changeAvatar(final User user, final MultipartFile avatar) {
        if (user.isAvatarSet()) {
            imageService.removeAvatar(user);
        }

        if (avatar != null) {
            String avatarImageName;
            try {
                avatarImageName = imageService.saveAvatar(avatar);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Cannot save avatar {0} for user {1}", new Object[] {avatar, user});
                return;
            }
            user.setAvatar(avatarImageName);
            repository.save(user);
        } else {
            user.setAvatar(null);
            repository.save(user);
        }
    }

    @Override
    public final void changePassword(final User user, final String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        repository.save(user);
    }

    @Override
    public final void changeEmail(final User user, final String newEmail) {
        user.setEmail(newEmail);
        user.setEmailConfirmed(false);
        repository.save(user);

        emailConfirmationService.sendEmailConfirmationRequest(user);
    }

    @Override
    public final User findUserByLogin(final String login) {
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
    public final User findUser(final String id) {
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
    public final void addTeam(final User user, final FruitonTeam teamToAdd) {
        // if team with the same name exists then remove it
        user.getTeams().removeIf(ft -> ft.getName().equals(teamToAdd.getName()));
        user.getTeams().add(teamToAdd);
        repository.save(user);
    }

    @Override
    public final void removeTeam(final User user, final String teamToRemove) {
        user.getTeams().removeIf(ft -> ft.getName().equals(teamToRemove));
        repository.save(user);
    }

}
