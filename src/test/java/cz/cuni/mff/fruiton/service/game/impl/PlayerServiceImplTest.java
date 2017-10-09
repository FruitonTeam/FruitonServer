package cz.cuni.mff.fruiton.service.game.impl;

import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.service.authentication.RegistrationService;
import cz.cuni.mff.fruiton.service.game.PlayerService;
import cz.cuni.mff.fruiton.test.util.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = cz.cuni.mff.fruiton.Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@PropertySource("classpath:game.properties")
public class PlayerServiceImplTest {

    @Value("#{'${default.unlocked.fruitons}'.split(',')}")
    private List<Integer> defaultUnlockedFruitons;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerService playerService;

    @Test
    public void testDefaultFruitons() {
        TestUtils.defaultRegister(registrationService, userRepository);

        List<Integer> playersFruitons = playerService.getAvailableFruitons(TestUtils.DEFAULT_LOGIN);
        for (int defaultUnlockedFruiton : defaultUnlockedFruitons) {
            assertTrue("Returned values must contain default values", playersFruitons.contains(defaultUnlockedFruiton));
        }

    }

}