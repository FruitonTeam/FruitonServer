package cz.cuni.mff.fruiton.service.social.impl;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.service.authentication.AuthenticationService;
import cz.cuni.mff.fruiton.service.authentication.RegistrationService;
import cz.cuni.mff.fruiton.test.util.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

@WebAppConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest(classes = cz.cuni.mff.fruiton.Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@PropertySource("classpath:game.properties")
public class UserServiceImplTest {

    private static final String AVATAR_NAME = "my_avatar.png";

    private static final String NEW_PASSWORD = "my_new_password";
    private static final String NEW_EMAIL = "my.new.email@email.com";

    @Value("#{'${default.unlocked.fruitons}'.split(',')}")
    private List<Integer> defaultUnlockedFruitons;

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
        userService.changeAvatar(UserIdHolder.of(user), TestUtils.getDefaultAvatar(AVATAR_NAME));

        assertTrue("Avatar was not set", userRepository.findById(user.getId()).get().isAvatarSet());

        userService.changeAvatar(UserIdHolder.of(user), (MultipartFile) null);

        assertFalse("Avatar was not removed", userRepository.findById(user.getId()).get().isAvatarSet());
    }

    @Test
    public void changePasswordTest() {
        userService.changePassword(UserIdHolder.of(user), NEW_PASSWORD);
        assertNotNull("Password was not changed correctly",
                authenticationService.authenticate(TestUtils.DEFAULT_LOGIN, NEW_PASSWORD));
    }

    @Test
    public void changeEmailTest() {
        userService.changeEmail(UserIdHolder.of(user), NEW_EMAIL);

        assertTrue("Email was not changed correctly",
                userRepository.findById(user.getId()).get().getEmail().equals(NEW_EMAIL));
    }

    @Test
    public void testDefaultFruitons() {
        List<Integer> playersFruitons = userService.getAvailableFruitons(UserIdHolder.of(user));
        for (int defaultUnlockedFruiton : defaultUnlockedFruitons) {
            assertTrue("Returned values must contain default values", playersFruitons.contains(defaultUnlockedFruiton));
        }
    }

}