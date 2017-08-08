package cz.cuni.mff.fruiton.service.authentication.impl;

import cz.cuni.mff.fruiton.dao.repository.EmailConfirmationRepository;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.dao.domain.MailConfirmation;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dto.UserProtos;
import cz.cuni.mff.fruiton.service.authentication.RegistrationService;
import cz.cuni.mff.fruiton.service.util.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@PropertySource("classpath:mail.properties")
public class RegistrationServiceImpl implements RegistrationService {

    private static final Logger logger = Logger.getLogger(RegistrationServiceImpl.class.getName());

    private final UserRepository userRepository;

    private final EmailConfirmationRepository mailConfirmationRepository;

    private final MailService mailService;

    private final PasswordEncoder passwordEncoder;

    @Value("${mail.confirmation.subject}")
    private String mailConfirmationSubject;

    @Value("${mail.confirmation.content.template}")
    private String mailConfirmationTemplate;

    @Autowired
    public RegistrationServiceImpl(
            final UserRepository userRepository,
            final EmailConfirmationRepository mailConfirmationRepository,
            final MailService mailService,
            final PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.mailConfirmationRepository = mailConfirmationRepository;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public final void register(final UserProtos.RegistrationData data) {
        if (data.getPassword() == null || data.getPassword().isEmpty()) {
            throw new RegistrationException("Cannot create hash from password: " + data.getPassword());
        }

        User user = new User()
                .withLogin(data.getLogin())
                .withPassword(passwordEncoder.encode(data.getPassword()))
                .withEmail(data.getEmail());

        userRepository.save(user);

        sendEmailConfirmationRequest(user);

        logger.log(Level.FINE, "Registered user: {0}", user);
    }

    private void sendEmailConfirmationRequest(final User user) {
        MailConfirmation confirmation = new MailConfirmation();
        confirmation.setUser(user);

        mailConfirmationRepository.save(confirmation);

        String mailConfirmationContent = MessageFormat.format(mailConfirmationTemplate, user.getLogin(), confirmation.getId());

        mailService.send(user.getEmail(), mailConfirmationSubject, mailConfirmationContent);
    }

    @Transactional
    public final void confirmEmail(final String confirmationId) {

        MailConfirmation confirmation = mailConfirmationRepository.findOne(confirmationId);
        if (confirmation == null) {
            throw new MailConfirmationNotFound("No MailConfirmation with id " + confirmationId);
        }

        User user = confirmation.getUser();
        if (user == null) {
            logger.log(Level.SEVERE, "No user for confirmation id: {0}", confirmationId);
            throw new RegistrationException("Cannot confirm email address, please contact support");
        }

        user.setEmailConfirmed(true);
        userRepository.save(user);

        mailConfirmationRepository.delete(confirmation);
    }

}
