package cz.cuni.mff.fruiton.service.game.impl;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.service.authentication.RegistrationService;
import cz.cuni.mff.fruiton.service.game.quest.impl.QuestServiceImpl;
import cz.cuni.mff.fruiton.test.util.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = cz.cuni.mff.fruiton.Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class QuestServiceImplTest {

    @Autowired
    private QuestServiceImpl questService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private UserRepository userRepository;

    @LocalServerPort
    private int port;

    @Test
    public void completeQuestTest() {
        User user = TestUtils.defaultRegister(registrationService, userRepository);

        // quests are assigned on login
        TestUtils.login(TestUtils.DEFAULT_LOGIN, TestUtils.DEFAULT_PASSWORD, port);

        questService.completeQuest(UserIdHolder.of(user), questService.getAllQuests(UserIdHolder.of(user)).get(0).getName());

        user = userRepository.findOne(user.getId());
        assertTrue("User must have received some money for completed quest", user.getMoney() > 0);
    }

}