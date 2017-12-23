package cz.cuni.mff.fruiton.service.game;

import cz.cuni.mff.fruiton.dao.domain.Achievement;
import cz.cuni.mff.fruiton.dao.domain.User;

import java.util.List;

public interface AchievementService {

    class AchievementStatusInfo {

        private Achievement achievement;

        private boolean unlocked = false;

        private int progress = 0;

        public Achievement getAchievement() {
            return achievement;
        }

        public void setAchievement(final Achievement achievement) {
            this.achievement = achievement;
        }

        public boolean isUnlocked() {
            return unlocked;
        }

        public void setUnlocked(final boolean unlocked) {
            this.unlocked = unlocked;
        }

        public int getProgress() {
            return progress;
        }

        public void setProgress(final int progress) {
            this.progress = progress;
        }
    }

    void updateAchievementProgress(User user, Achievement achievement, int incrementValue);

    void updateAchievementProgress(User user, String achievementName, int incrementValue);

    void unlockAchievement(User user, Achievement achievement);

    void unlockAchievement(User user, String achievementName);

    List<AchievementStatusInfo> getAchievementStatusesForUser(User user);

}
