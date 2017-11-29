package cz.cuni.mff.fruiton.service.authentication.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dto.UserProtos;
import cz.cuni.mff.fruiton.dto.form.RegistrationForm;
import cz.cuni.mff.fruiton.service.authentication.RegistrationService;
import cz.cuni.mff.fruiton.service.game.QuestService;
import cz.cuni.mff.fruiton.service.social.EmailConfirmationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@PropertySource("classpath:mail.properties")
public final class RegistrationServiceImpl implements RegistrationService {

    private static final int RANDOM_GOOGLE_SUFFIX_SIZE = 5;

    private static final int RANDOM_GOOGLE_PASSWORD_SIZE = 10;

    private static final Logger logger = Logger.getLogger(RegistrationServiceImpl.class.getName());

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailConfirmationService emailConfirmationService;

    private final QuestService questService;

    @Autowired
    public RegistrationServiceImpl(
            final UserRepository userRepository,
            final EmailConfirmationService emailConfirmationService,
            final PasswordEncoder passwordEncoder,
            final QuestService questService
    ) {
        this.userRepository = userRepository;
        this.emailConfirmationService = emailConfirmationService;
        this.passwordEncoder = passwordEncoder;
        this.questService = questService;
    }

    @Override
    public void register(final UserProtos.RegistrationData data) {
        if (data.getPassword() == null || data.getPassword().isEmpty()) {
            throw new RegistrationException("Cannot register user with password: " + data.getPassword());
        }

        User user = new User()
                .withLogin(data.getLogin())
                .withPassword(passwordEncoder.encode(data.getPassword()))
                .withEmail(data.getEmail());

        saveUser(user);
        emailConfirmationService.sendEmailConfirmationRequest(user);
    }

    @Override
    public User register(final RegistrationForm form) {
        User user = new User()
                .withLogin(form.getLogin())
                .withPassword(passwordEncoder.encode(form.getPassword()))
                .withEmail(form.getEmail());

        saveUser(user);
        emailConfirmationService.sendEmailConfirmationRequest(user);

        return user;
    }

    @Override
    public User register(final GoogleIdToken.Payload payload) {
        String login = getGoogleLogin(payload);

        User user = new User()
                .withLogin(login)
                .withPassword(passwordEncoder.encode(StringUtils.randomAlphanumeric(RANDOM_GOOGLE_PASSWORD_SIZE)))
                .withEmail(payload.getEmail());

        user.setGoogleSubject(payload.getSubject());

        saveUser(user);
        return user;
    }

    private void saveUser(final User user) {
        userRepository.save(user);
        onRegistered(user);
    }

    private void onRegistered(final User user) {
        logger.log(Level.FINE, "Registered user: {0}", user);

        questService.assignNewQuests(user);
    }

    private String getGoogleLogin(final GoogleIdToken.Payload payload) {
        String login = "google" + payload.getSubject();
        while (userRepository.findByLogin(login) != null) {
            login += StringUtils.randomAlphanumeric(RANDOM_GOOGLE_SUFFIX_SIZE);
        }

        return login;
    }

}
