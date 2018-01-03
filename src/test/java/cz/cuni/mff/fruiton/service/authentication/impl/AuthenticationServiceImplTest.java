package cz.cuni.mff.fruiton.service.authentication.impl;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.UserProtos.RegistrationData;
import cz.cuni.mff.fruiton.service.authentication.RegistrationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static cz.cuni.mff.fruiton.test.util.TestUtils.getRegistrationData;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = cz.cuni.mff.fruiton.Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AuthenticationServiceImplTest {

    private static final String EMAIL = "test@test.com";
    private static final String LOGIN = "login";
    private static final String PASSWORD = "password";

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private AuthenticationServiceImpl authenticationService;

    @Test
    public void authenticateTest() {
        RegistrationData data = getRegistrationData(EMAIL, LOGIN, PASSWORD);
        registrationService.register(data);

        UserIdHolder user = authenticationService.authenticate(LOGIN, PASSWORD);
        assertNotNull("Authentication fail", user);
        assertEquals("Login should be equal", LOGIN, user.getUsername());
    }

    @Test(expected = UsernameNotFoundException.class)
    public void authenticateNotRegisteredUserTest() {
        authenticationService.authenticate("unknownLogin", PASSWORD);
    }

    @Test(expected = BadCredentialsException.class)
    public void badCredentialsTest() {
        RegistrationData data = getRegistrationData(EMAIL, LOGIN, PASSWORD);
        registrationService.register(data);
        authenticationService.authenticate(LOGIN, "badPassword");
    }

}