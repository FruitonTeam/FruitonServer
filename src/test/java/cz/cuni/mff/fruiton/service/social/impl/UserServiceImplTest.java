package cz.cuni.mff.fruiton.service.social.impl;

import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.service.authentication.AuthenticationService;
import cz.cuni.mff.fruiton.service.authentication.RegistrationService;
import cz.cuni.mff.fruiton.test.util.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = cz.cuni.mff.fruiton.Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserServiceImplTest {

    private static final String AVATAR_NAME = "my_avatar.png";

    private static final String NEW_PASSWORD = "my_new_password";
    private static final String NEW_EMAIL = "my.new.email@email.com";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private AuthenticationService authenticationService;

    private User user;

    @Before
    public void setup() {
        user = TestUtils.defaultRegister(registrationService, userRepository);
    }

    @Test
    public void changeAvatarTest() throws IOException {
        userService.changeAvatar(user, TestUtils.getDefaultAvatar(AVATAR_NAME));

        assertTrue("Avatar was not set", userService.findUser(user.getId()).isAvatarSet());

        userService.changeAvatar(user, (MultipartFile) null);

        assertFalse("Avatar was not removed", userService.findUser(user.getId()).isAvatarSet());
    }

    @Test
    public void changePasswordTest() throws IOException {
        userService.changePassword(user, NEW_PASSWORD);
        assertNotNull("Password was not changed correctly",
                authenticationService.authenticate(TestUtils.DEFAULT_LOGIN, NEW_PASSWORD));
    }

    @Test
    public void changeEmailTest() {
        userService.changeEmail(user, NEW_EMAIL);

        assertTrue("Email was not changed correctly",
                userService.findUser(user.getId()).getEmail().equals(NEW_EMAIL));
    }

}