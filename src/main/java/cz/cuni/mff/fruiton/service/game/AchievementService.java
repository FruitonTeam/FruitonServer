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

    /**
     * Updates progress of the specified achievement.
     * @param user user whose progress will be updated
     * @param achievement achievement which progress will be updated
     * @param incrementValue value by which the progress will be incremented
     */
    void updateAchievementProgress(UserIdHolder user, Achievement achievement, int incrementValue);

    /**
     * Updates progress of the specified achievement.
     * @param user user whose progress will be updated
     * @param achievementName name of the achievement which progress will be updated
     * @param incrementValue value by which the progress will be incremented
     */
    void updateAchievementProgress(UserIdHolder user, String achievementName, int incrementValue);

    /**
     * Unlocks specified achievement for specified user.
     * @param user user for whom to unlock the achievement
     * @param achievement achievement to unlock
     */
    void unlockAchievement(UserIdHolder user, Achievement achievement);

    /**
     * Unlocks specified achievement for specified user.
     * @param user user for whom to unlock the achievement
     * @param achievementName name of the achievement to unlock
     */
    void unlockAchievement(UserIdHolder user, String achievementName);

    /**
     * Returns achievements progress for specified user.
     * @param user user for whom to get achievements progress
     * @return achievements progress for specified user
     */
    List<AchievementStatusInfo> getAchievementStatusesForUser(UserIdHolder user);

}
