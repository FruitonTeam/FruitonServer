package cz.cuni.mff.fruiton.service.authentication.impl;

import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.service.authentication.PasswordService;
import cz.cuni.mff.fruiton.service.communication.mail.MailService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@PropertySource("classpath:mail.properties")
public class PasswordServiceImpl implements PasswordService {

    private static final int NEW_PASSWORD_LENGTH = 10;

    private static final Logger logger = Logger.getLogger(PasswordServiceImpl.class.getName());

    @Value("${mail.password.renew.subject}")
    private String subject;

    @Value("${mail.password.renew.content}")
    private String contentTemplate;

    private final UserRepository userRepository;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PasswordServiceImpl(UserRepository userRepository, MailService mailService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
    }

    /** {@inheritDoc} */
    @Override
    public void renew(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Cannot renew password for null email");
        }

        logger.log(Level.FINEST, "Renewing password for {0}", email);

        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("No user with email " + email + " found");
        }

        String newPasswd = RandomStringUtils.randomAlphanumeric(NEW_PASSWORD_LENGTH);
        String encodedPasswd = passwordEncoder.encode(newPasswd);

        user.setPassword(encodedPasswd);

        userRepository.save(user);

        mailService.send(email, subject, MessageFormat.format(contentTemplate, user.getLogin(), newPasswd));

        logger.log(Level.FINEST, "Password renewed successfully for {0}", user);
    }

}
