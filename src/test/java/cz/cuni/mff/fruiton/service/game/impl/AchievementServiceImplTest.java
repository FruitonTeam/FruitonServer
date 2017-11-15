package cz.cuni.mff.fruiton.service.game.impl;

import cz.cuni.mff.fruiton.dao.domain.Achievement;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dao.repository.AchievementRepository;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.service.authentication.RegistrationService;
import cz.cuni.mff.fruiton.test.util.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = cz.cuni.mff.fruiton.Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AchievementServiceImplTest {

    private static final String ACHIEVEMENT_NAME = "Sprinter";

    @Autowired
    private AchievementServiceImpl achievementService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    @Test
    public void unlockAchievementTest() {
        User user = TestUtils.defaultRegister(registrationService, userRepository);
        achievementService.unlockAchievement(user, ACHIEVEMENT_NAME);

        user = userRepository.findOne(user.getId());
        assertThat(user.getUnlockedAchievements(), hasItems(achievementRepository.findByName(ACHIEVEMENT_NAME)));
    }

    @Test
    public void unlockProgressAchievementTest() {
        User user = TestUtils.defaultRegister(registrationService, userRepository);
        Achievement achievement = achievementRepository.findByName(ACHIEVEMENT_NAME);

        for (int i = 0; i < achievement.getGoal(); i++) {
            achievementService.updateAchievementProgress(user, achievement, 1);
        }

        user = userRepository.findOne(user.getId());
        assertThat(user.getUnlockedAchievements(), hasItems(achievementRepository.findByName(ACHIEVEMENT_NAME)));
    }

}