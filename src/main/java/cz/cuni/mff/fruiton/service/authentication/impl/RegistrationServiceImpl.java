package cz.cuni.mff.fruiton.service.authentication.impl;

import cz.cuni.mff.fruiton.dao.EmailConfirmationRepository;
import cz.cuni.mff.fruiton.dao.UserRepository;
import cz.cuni.mff.fruiton.dao.model.MailConfirmation;
import cz.cuni.mff.fruiton.dao.model.User;
import cz.cuni.mff.fruiton.dto.UserProtos;
import cz.cuni.mff.fruiton.service.authentication.PasswordService;
import cz.cuni.mff.fruiton.service.authentication.RegistrationService;
import cz.cuni.mff.fruiton.service.util.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.spec.InvalidKeySpecException;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@PropertySource("classpath:mail.properties")
public class RegistrationServiceImpl implements RegistrationService {

    private static final Logger logger = Logger.getLogger(RegistrationServiceImpl.class.getName());

    private final UserRepository userRepository;

    private final EmailConfirmationRepository mailConfirmationRepository;

    private final PasswordService passwdService;

    private final MailService mailService;

    @Value("${mail.confirmation.subject}")
    private String mailConfirmationSubject;

    @Value("${mail.confirmation.content.template}")
    private String mailConfirmationTemplate;

    @Autowired
    public RegistrationServiceImpl(
            final UserRepository userRepository,
            final EmailConfirmationRepository mailConfirmationRepository,
            final PasswordService passwdService,
            final MailService mailService
    ) {
        this.userRepository = userRepository;
        this.mailConfirmationRepository = mailConfirmationRepository;
        this.passwdService = passwdService;
        this.mailService = mailService;
    }

    @Transactional
    public final void register(final UserProtos.RegistrationData data) {

        PasswordServiceImpl.Hash passwdHash;
        try {
            passwdHash = passwdService.getPasswordHash(data.getPassword());
        } catch (IllegalArgumentException e) {
            throw new RegistrationException("Cannot create hash from password: " + data.getPassword());
        } catch (InvalidKeySpecException e) {
            logger.log(Level.SEVERE, "Could not create password hash", e);
            throw new RegistrationException("Cannot register user because of the internal error");
        }

        User user = new User()
                .withLogin(data.getLogin())
                .withPasswordHash(passwdHash.getHash())
                .withPasswordSalt(passwdHash.getSalt())
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
