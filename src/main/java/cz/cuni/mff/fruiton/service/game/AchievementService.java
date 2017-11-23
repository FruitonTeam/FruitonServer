package cz.cuni.mff.fruiton.service.game;

import cz.cuni.mff.fruiton.dao.domain.Achievement;
import cz.cuni.mff.fruiton.dao.domain.User;

public interface AchievementService {

    void updateAchievementProgress(User user, Achievement achievement, int incrementValue);

    void updateAchievementProgress(User user, String achievementName, int incrementValue);

    void unlockAchievement(User user, Achievement achievement);

    void unlockAchievement(User user, String achievementName);

}
