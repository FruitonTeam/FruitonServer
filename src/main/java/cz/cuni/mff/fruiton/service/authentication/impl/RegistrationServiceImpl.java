package cz.cuni.mff.fruiton.service.authentication.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import cz.cuni.mff.fruiton.component.util.ServerAddressHelper;
import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dto.UserProtos;
import cz.cuni.mff.fruiton.dto.form.RegistrationForm;
import cz.cuni.mff.fruiton.service.authentication.RegistrationService;
import cz.cuni.mff.fruiton.service.communication.mail.MailService;
import cz.cuni.mff.fruiton.service.game.quest.QuestService;
import cz.cuni.mff.fruiton.service.social.EmailConfirmationService;
import cz.cuni.mff.fruiton.service.social.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@PropertySource("classpath:mail.properties")
public final class RegistrationServiceImpl implements RegistrationService {

    private static final int RANDOM_GOOGLE_PASSWORD_SIZE = 10;

    private static final String RENEW_PASSWORD_URL = "resetPassword";

    private static final Logger logger = Logger.getLogger(RegistrationServiceImpl.class.getName());

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailConfirmationService emailConfirmationService;

    private final UserService userService;

    private final MailService mailService;

    private final QuestService questService;

    private final ServerAddressHelper serverAddressHelper;

    @Value("${mail.google.welcome.subject}")
    private String googleWelcomeMailSubject;

    @Value("${mail.google.welcome.template}")
    private String googleWelcomeMailTemplate;

    @Autowired
    public RegistrationServiceImpl(
            final UserRepository userRepository,
            final EmailConfirmationService emailConfirmationService,
            final PasswordEncoder passwordEncoder,
            final UserService userService,
            final MailService mailService,
            final QuestService questService,
            final ServerAddressHelper serverAddressHelper
    ) {
        this.userRepository = userRepository;
        this.emailConfirmationService = emailConfirmationService;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.mailService = mailService;
        this.questService = questService;
        this.serverAddressHelper = serverAddressHelper;
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
    public UserIdHolder register(final RegistrationForm form) {
        User user = new User()
                .withLogin(form.getLogin())
                .withPassword(passwordEncoder.encode(form.getPassword()))
                .withEmail(form.getEmail());

        saveUser(user);
        emailConfirmationService.sendEmailConfirmationRequest(user);

        return UserIdHolder.of(user);
    }

    @Override
    public UserIdHolder register(final String login, final GoogleIdToken.Payload payload) {
        User user = new User()
                .withLogin(login)
                .withPassword(passwordEncoder.encode(StringUtils.randomAlphanumeric(RANDOM_GOOGLE_PASSWORD_SIZE)))
                .withEmail(payload.getEmail());

        user.setGoogleSubject(payload.getSubject());
        questService.assignNewQuests(user);

        saveUser(user);

        getGooglePictureUrl(payload).ifPresentOrElse(url -> userService.changeAvatar(UserIdHolder.of(user), url),
                () -> logger.log(Level.FINER,
                        "User {0} does not have google avatar, using default one", user));

        mailService.send(payload.getEmail(), googleWelcomeMailSubject,
                MessageFormat.format(googleWelcomeMailTemplate, login, serverAddressHelper.getHttpAddress(RENEW_PASSWORD_URL)));

        return UserIdHolder.of(user);
    }

    private void saveUser(final User user) {
        userRepository.save(user);
        onRegistered(user);
    }

    private void onRegistered(final User user) {
        logger.log(Level.FINE, "Registered user: {0}", user);
    }

    private Optional<String> getGooglePictureUrl(final GoogleIdToken.Payload payload) {
        String pictureUrl = (String) payload.get("picture");
        if (pictureUrl != null) {
            return Optional.of(pictureUrl);
        } else {
            return Optional.empty();
        }
    }

}
