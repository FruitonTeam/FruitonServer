package cz.cuni.mff.fruiton.service.authentication.impl;

import cz.cuni.mff.fruiton.dao.repository.EmailConfirmationRepository;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.dao.domain.MailConfirmation;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dto.UserProtos.RegistrationData;
import cz.cuni.mff.fruiton.service.authentication.RegistrationService.RegistrationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolationException;

import static cz.cuni.mff.fruiton.test.util.TestUtils.getRegistrationData;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = cz.cuni.mff.fruiton.Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RegistrationServiceImplTest {

    private static final String EMAIL = "test@test.com";
    private static final String LOGIN = "login";
    private static final String PASSWORD = "password";


    @Autowired
    private RegistrationServiceImpl registrationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailConfirmationRepository confirmationRepository;

    @Test
    public void registerTest() {
        RegistrationData data = getRegistrationData(EMAIL, LOGIN, PASSWORD);

        registrationService.register(data);

        User user = userRepository.findByLogin(LOGIN);
        assertNotNull("User was not persisted after registration", user);
        assertEquals("Persisted email is different than the one used for registration", EMAIL, user.getEmail());
    }

    @Test(expected = DuplicateKeyException.class)
    public void registerWithSameLogin() {
        RegistrationData data = getRegistrationData(EMAIL, LOGIN, PASSWORD);

        registrationService.register(data);
        registrationService.register(data);
    }

    @Test
    public void confirmMailTest() {
        RegistrationData data = getRegistrationData(EMAIL, LOGIN, PASSWORD);

        registrationService.register(data);

        User user = userRepository.findByLogin(LOGIN);

        assertFalse("Email should not be confirmed yet", user.isEmailConfirmed());

        String id = getMailConfirmationIdForUser(user);

        registrationService.confirmEmail(id);

        user = userRepository.findByLogin(LOGIN); // refresh data
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

    @Test(expected = ConstraintViolationException.class)
    public void registerWithEmptyLoginTest() {
        RegistrationData data = getRegistrationData(EMAIL, "", PASSWORD);
        registrationService.register(data);
    }

    @Test(expected = ConstraintViolationException.class)
    public void registerWithShortLoginTest() {
        RegistrationData data = getRegistrationData(EMAIL, "a", PASSWORD);
        registrationService.register(data);
    }

    @Test(expected = ConstraintViolationException.class)
    public void registerWithWrongEmailTest() {
        RegistrationData data = getRegistrationData("test", LOGIN, PASSWORD);
        registrationService.register(data);
    }

    @Test(expected = ConstraintViolationException.class)
    public void registerWithEmptyEmailTest() {
        RegistrationData data = getRegistrationData("", LOGIN, PASSWORD);
        registrationService.register(data);
    }

    @Test(expected = RegistrationException.class)
    public void registerWithEmptyPasswordTest() {
        RegistrationData data = getRegistrationData(EMAIL, LOGIN, "");
        registrationService.register(data);
    }

    @Test(expected = ConstraintViolationException.class)
    public void registerWithNonAlphanumericLoginTest() {
        RegistrationData data = getRegistrationData(EMAIL, "login\n\r", PASSWORD);
        registrationService.register(data);
    }

}