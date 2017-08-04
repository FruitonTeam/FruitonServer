package cz.cuni.mff.fruiton.service.authentication.impl;

import cz.cuni.mff.fruiton.dao.EmailConfirmationRepository;
import cz.cuni.mff.fruiton.dao.UserRepository;
import cz.cuni.mff.fruiton.dao.model.MailConfirmation;
import cz.cuni.mff.fruiton.dao.model.User;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = cz.cuni.mff.fruiton.Application.class)
@DirtiesContext
public class RegistrationServiceImplTest {

    @Autowired
    private RegistrationServiceImpl registrationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailConfirmationRepository confirmationRepository;

    @Test
    public void registerTest() {
        RegistrationData data = RegistrationData.newBuilder()
                .setEmail("test@test.com")
                .setLogin("login")
                .setPassword("password")
                .build();

        registrationService.register(data);

        User user = userRepository.findByLogin("login");
        assertNotNull("User was not persisted after registration", user);
        assertEquals("Persisted email is different than the one used for registration", "test@test.com", user.getEmail());
    }

    @Test(expected = DuplicateKeyException.class)
    public void registerWithSameLogin() {
        RegistrationData data = RegistrationData.newBuilder()
                .setEmail("test1@test.com")
                .setLogin("login1")
                .setPassword("password")
                .build();

        registrationService.register(data);
        registrationService.register(data);
    }

    @Test
    public void confirmMailTest() {
        RegistrationData data = RegistrationData.newBuilder()
                .setEmail("test1@test.com")
                .setLogin("login2")
                .setPassword("password")
                .build();

        registrationService.register(data);

        User user = userRepository.findByLogin("login2");

        assertFalse("Email should not be confirmed yet", user.isEmailConfirmed());

        String id = getMailConfirmationIdForUser(user);

        registrationService.confirmEmail(id);

        user = userRepository.findByLogin("login2"); // refresh data
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
        RegistrationData data = RegistrationData.newBuilder()
                .setEmail("test1@test.com")
                .setLogin("")
                .setPassword("password")
                .build();
        registrationService.register(data);
    }

    @Test(expected = ConstraintViolationException.class)
    public void registerWithShortLoginTest() {
        RegistrationData data = RegistrationData.newBuilder()
                .setEmail("test1@test.com")
                .setLogin("a")
                .setPassword("password")
                .build();
        registrationService.register(data);
    }

    @Test(expected = ConstraintViolationException.class)
    public void registerWithWrongEmailTest() {
        RegistrationData data = RegistrationData.newBuilder()
                .setEmail("test")
                .setLogin("login3")
                .setPassword("password")
                .build();
        registrationService.register(data);
    }

    @Test(expected = ConstraintViolationException.class)
    public void registerWithEmptyEmailTest() {
        RegistrationData data = RegistrationData.newBuilder()
                .setEmail("")
                .setLogin("login4")
                .setPassword("password")
                .build();
        registrationService.register(data);
    }

    @Test(expected = RegistrationException.class)
    public void registerWithEmptyPasswordTest() {
        RegistrationData data = RegistrationData.newBuilder()
                .setEmail("test@test.com")
                .setLogin("login6")
                .setPassword("")
                .build();
        registrationService.register(data);
    }

    @Test(expected = ConstraintViolationException.class)
    public void registerWithNonAlphanumericLoginTest() {
        RegistrationData data = RegistrationData.newBuilder()
                .setEmail("test@test.com")
                .setLogin("myLogin\n\r")
                .setPassword("password")
                .build();
        registrationService.register(data);
    }

}