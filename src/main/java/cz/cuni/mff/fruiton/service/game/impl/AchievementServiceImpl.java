package cz.cuni.mff.fruiton.service.game.impl;

import cz.cuni.mff.fruiton.dao.domain.Achievement;
import cz.cuni.mff.fruiton.dao.domain.AchievementProgress;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dao.repository.AchievementProgressRepository;
import cz.cuni.mff.fruiton.dao.repository.AchievementRepository;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.service.communication.CommunicationService;
import cz.cuni.mff.fruiton.service.game.AchievementService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public final class AchievementServiceImpl implements AchievementService {

    private static final String ACHIEVEMENT_UNLOCKED_NOTIFICATION_TITLE = "Achievement unlocked";

    private static final Logger logger = Logger.getLogger(AchievementServiceImpl.class.getName());

    private final UserRepository userRepository;
    private final AchievementRepository achievementRepository;
    private final AchievementProgressRepository achievementProgressRepository;

    private final CommunicationService communicationService;

    @Autowired
    public AchievementServiceImpl(
            final UserRepository userRepository,
            final AchievementRepository achievementRepository,
            final AchievementProgressRepository achievementProgressRepository,
            final CommunicationService communicationService
    ) {
        this.userRepository = userRepository;
        this.achievementRepository = achievementRepository;
        this.achievementProgressRepository = achievementProgressRepository;
        this.communicationService = communicationService;
    }

    @Override
    public void updateAchievementProgress(final User user, final Achievement achievement, final int incrementValue) {
        if (user == null) {
            throw new IllegalArgumentException("Cannot update achievement progress for null user");
        }
        if (achievement == null) {
            throw new IllegalArgumentException("Cannot update achievement progress for null achievement");
        }
        if (user.getUnlockedAchievements().contains(achievement)) {
            return;
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
    public void updateAchievementProgress(final User user, final String achievementName, final int incrementValue) {
        Achievement achievement = achievementRepository.findByName(achievementName);
        unlockAchievement(user, achievement);
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

        communicationService.sendNotification(user, getBase64AchievementImage(achievement),
                ACHIEVEMENT_UNLOCKED_NOTIFICATION_TITLE, achievement.getName());
    }

    private String getBase64AchievementImage(final Achievement achievement) {
        try {
            return Base64.getEncoder().encodeToString(
                    IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream(
                            "static/img/achievement/" + achievement.getImage())));
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not get achievement image ", e);
        }
        return "";
    }

    @Override
    public void unlockAchievement(final User user, final String achievementName) {
        Achievement achievement = achievementRepository.findByName(achievementName);
        unlockAchievement(user, achievement);
    }

}
