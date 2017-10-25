package cz.cuni.mff.fruiton.service.social.impl;

import cz.cuni.mff.fruiton.dao.domain.MailConfirmation;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dao.repository.EmailConfirmationRepository;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.service.authentication.RegistrationService;
import cz.cuni.mff.fruiton.test.util.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = cz.cuni.mff.fruiton.Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class EmailConfirmationServiceImplTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailConfirmationRepository confirmationRepository;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private EmailConfirmationServiceImpl emailConfirmationService;

    @Test
    public void confirmMailTest() {
        User user = TestUtils.defaultRegister(registrationService, userRepository);

        assertFalse("Email should not be confirmed yet", user.isEmailConfirmed());

        String id = getMailConfirmationIdForUser(user);

        emailConfirmationService.confirmEmail(id);

        user = userRepository.findByLogin(TestUtils.DEFAULT_LOGIN); // refresh data
        assertTrue("Email should be confirmed", user.isEmailConfirmed());
    }

    private String getMailConfirmationIdForUser(final User user) {
        for (MailConfirmation mailConfirmation : confirmationRepository.findAll()) {
            if (user.equals(mailConfirmation.getUser())) {
                return mailConfirmation.getId();
            }
        }
        return null;
    }

}