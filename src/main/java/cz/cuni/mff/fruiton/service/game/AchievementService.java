package cz.cuni.mff.fruiton.service.game;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dao.domain.Achievement;

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

    void updateAchievementProgress(UserIdHolder user, Achievement achievement, int incrementValue);

    void updateAchievementProgress(UserIdHolder user, String achievementName, int incrementValue);

    void unlockAchievement(UserIdHolder user, Achievement achievement);

    void unlockAchievement(UserIdHolder user, String achievementName);

    List<AchievementStatusInfo> getAchievementStatusesForUser(UserIdHolder user);

}
