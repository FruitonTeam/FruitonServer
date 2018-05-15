package cz.cuni.mff.fruiton.service.social.impl;

import cz.cuni.mff.fruiton.component.util.ServerAddressHelper;
import cz.cuni.mff.fruiton.dao.domain.MailConfirmation;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dao.repository.EmailConfirmationRepository;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.service.authentication.RegistrationService;
import cz.cuni.mff.fruiton.service.communication.mail.MailService;
import cz.cuni.mff.fruiton.service.social.EmailConfirmationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class EmailConfirmationServiceImpl implements EmailConfirmationService {

    private static final String MAIL_CONFIRM_URL = "api/confirmMail?confirmationId=";

    private static final Logger logger = Logger.getLogger(EmailConfirmationServiceImpl.class.getName());

    private final UserRepository userRepository;
    private final EmailConfirmationRepository mailConfirmationRepository;

    private final MailService mailService;

    private final ServerAddressHelper serverAddressHelper;

    @Value("${mail.confirmation.subject}")
    private String mailConfirmationSubject;

    @Value("${mail.confirmation.content.template}")
    private String mailConfirmationTemplate;

    public EmailConfirmationServiceImpl(
            final UserRepository userRepository,
            final EmailConfirmationRepository mailConfirmationRepository,
            final MailService mailService,
            final ServerAddressHelper serverAddressHelper
    ) {
        this.userRepository = userRepository;
        this.mailConfirmationRepository = mailConfirmationRepository;
        this.mailService = mailService;
        this.serverAddressHelper = serverAddressHelper;
    }

    @Override
    public final void sendEmailConfirmationRequest(final User user) {
        MailConfirmation confirmation = new MailConfirmation();
        confirmation.setUser(user);

        mailConfirmationRepository.save(confirmation);

        String mailConfirmationContent = MessageFormat.format(mailConfirmationTemplate, user.getLogin(),
                serverAddressHelper.getHttpAddress(MAIL_CONFIRM_URL + confirmation.getId()));

        mailService.send(user.getEmail(), mailConfirmationSubject, mailConfirmationContent);
    }

    @Override
    public final void confirmEmail(final String confirmationId) {

        Optional<MailConfirmation> confirmation = mailConfirmationRepository.findById(confirmationId);
        if (!confirmation.isPresent()) {
            throw new MailConfirmationNotFound("No MailConfirmation with id " + confirmationId);
        }

        User user = confirmation.get().getUser();
        if (user == null) {
            logger.log(Level.SEVERE, "No user for confirmation id: {0}", confirmationId);
            throw new RegistrationService.RegistrationException("Cannot confirm email address, please contact support");
        }

        user.setEmailConfirmed(true);
        userRepository.save(user);

        mailConfirmationRepository.deleteById(confirmationId);
    }

}
