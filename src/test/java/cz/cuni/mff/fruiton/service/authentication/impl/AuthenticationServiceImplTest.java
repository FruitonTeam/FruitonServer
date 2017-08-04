package cz.cuni.mff.fruiton.service.authentication.impl;

import cz.cuni.mff.fruiton.dao.model.User;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = cz.cuni.mff.fruiton.Application.class)
@DirtiesContext
public class AuthenticationServiceImplTest {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private AuthenticationServiceImpl authenticationService;

    @Test
    public void authenticateTest() {
        RegistrationData data = RegistrationData.newBuilder()
                .setEmail("test@test.com")
                .setLogin("login")
                .setPassword("password")
                .build();
        registrationService.register(data);

        User user = authenticationService.authenticate("login", "password");
        assertNotNull("Authentication fail", user);
        assertEquals("Login should be equal", "login", user.getLogin());
    }

    @Test(expected = UsernameNotFoundException.class)
    public void authenticateNotRegisteredUserTest() {
        authenticationService.authenticate("unknownLogin", "password");
    }

    @Test(expected = BadCredentialsException.class)
    public void badCredentialsTest() {
        RegistrationData data = RegistrationData.newBuilder()
                .setEmail("test@test.com")
                .setLogin("login2")
                .setPassword("password")
                .build();
        registrationService.register(data);
        authenticationService.authenticate("login2", "badPassword");
    }

}