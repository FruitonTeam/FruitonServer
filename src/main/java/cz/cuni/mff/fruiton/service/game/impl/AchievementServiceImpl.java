package cz.cuni.mff.fruiton.service.game.impl;

import cz.cuni.mff.fruiton.dao.domain.Achievement;
import cz.cuni.mff.fruiton.dao.domain.AchievementProgress;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dao.repository.AchievementProgressRepository;
import cz.cuni.mff.fruiton.dao.repository.AchievementRepository;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.service.game.AchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public final class AchievementServiceImpl implements AchievementService {

    private final UserRepository userRepository;
    private final AchievementRepository achievementRepository;
    private final AchievementProgressRepository achievementProgressRepository;

    @Autowired
    public AchievementServiceImpl(
            final UserRepository userRepository,
            final AchievementRepository achievementRepository,
            final AchievementProgressRepository achievementProgressRepository
    ) {
        this.userRepository = userRepository;
        this.achievementRepository = achievementRepository;
        this.achievementProgressRepository = achievementProgressRepository;
    }

    @Override
    public void updateAchievementProgress(final User user, final Achievement achievement, final int incrementValue) {
        if (user == null) {
            throw new IllegalArgumentException("Cannot update achievement progress for null user");
        }
        if (achievement == null) {
            throw new IllegalArgumentException("Cannot update achievement progress for null achievement");
        }
        if (incrementValue < 0) {
            throw new IllegalArgumentException("Cannot decrease achievement progress");
        }
        if (incrementValue == 0) {
            return;
        }

        AchievementProgress achievementProgress = achievementProgressRepository.findByUserAndAchievement(user, achievement);
        if (achievementProgress == null) {
            achievementProgress = new AchievementProgress(user, achievement);
        }

        achievementProgress.incrementProgress(incrementValue);

        if (achievementProgress.isCompleted()) {
            if (achievementProgress.getId() != null) { // progress is stored in db, we need to delete it
                achievementProgressRepository.delete(achievementProgress);
            }
            unlockAchievement(user, achievement);
        } else {
            achievementProgressRepository.save(achievementProgress);
        }
    }

    @Override
    public void unlockAchievement(final User user, final Achievement achievement) {
        if (user == null) {
            throw new IllegalArgumentException("Cannot unlock achievement for null user");
        }
        if (achievement == null) {
            throw new IllegalArgumentException("Cannot unlock achievement for null achievement");
        }
        user.getUnlockedAchievements().add(achievement);
        userRepository.save(user);
    }

    @Override
    public void unlockAchievement(final User user, final String achievementName) {
        Achievement achievement = achievementRepository.findByName(achievementName);
        unlockAchievement(user, achievement);
    }

}
