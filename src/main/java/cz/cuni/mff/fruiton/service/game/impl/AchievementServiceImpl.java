package cz.cuni.mff.fruiton.service.game.impl;

import cz.cuni.mff.fruiton.component.util.ResourceHelper;
import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dao.domain.Achievement;
import cz.cuni.mff.fruiton.dao.domain.AchievementProgress;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dao.repository.AchievementProgressRepository;
import cz.cuni.mff.fruiton.dao.repository.AchievementRepository;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.service.communication.CommunicationService;
import cz.cuni.mff.fruiton.service.game.AchievementService;
import cz.cuni.mff.fruiton.service.social.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
    private final UserService userService;

    private final ResourceHelper resourceHelper;

    @Autowired
    public AchievementServiceImpl(
            final UserRepository userRepository,
            final AchievementRepository achievementRepository,
            final AchievementProgressRepository achievementProgressRepository,
            final CommunicationService communicationService,
            final ResourceHelper resourceHelper,
            final UserService userService
    ) {
        this.userRepository = userRepository;
        this.achievementRepository = achievementRepository;
        this.achievementProgressRepository = achievementProgressRepository;
        this.communicationService = communicationService;
        this.resourceHelper = resourceHelper;
        this.userService = userService;
    }

    @Override
    public void updateAchievementProgress(final UserIdHolder idHolder, final Achievement achievement, final int incrementValue) {
        if (idHolder == null) {
            throw new IllegalArgumentException("Cannot update achievement progress for null user");
        }
        if (achievement == null) {
            throw new IllegalArgumentException("Cannot update achievement progress for null achievement");
        }
        if (userService.getUnlockedAchievements(idHolder).contains(achievement)) {
            return;
        }
        if (incrementValue < 0) {
            throw new IllegalArgumentException("Cannot decrease achievement progress");
        }
        if (incrementValue == 0) {
            return;
        }

        User user = userRepository.findOne(idHolder.getId());

        AchievementProgress achievementProgress = achievementProgressRepository.findByUserAndAchievement(user, achievement);
        if (achievementProgress == null) {
            achievementProgress = new AchievementProgress(user, achievement);
        }

        achievementProgress.incrementProgress(incrementValue);

        if (achievementProgress.isCompleted()) {
            if (achievementProgress.getId() != null) { // progress is stored in db, we need to delete it
                achievementProgressRepository.delete(achievementProgress);
            }
            userService.unlockAchievement(idHolder, achievement);
        } else {
            achievementProgressRepository.save(achievementProgress);
        }
    }

    @Override
    public void updateAchievementProgress(final UserIdHolder user, final String achievementName, final int incrementValue) {
        Achievement achievement = achievementRepository.findByName(achievementName);
        updateAchievementProgress(user, achievement, incrementValue);
    }

    @Override
    public void unlockAchievement(final UserIdHolder user, final Achievement achievement) {
        if (user == null) {
            throw new IllegalArgumentException("Cannot unlock achievement for null user");
        }
        if (achievement == null) {
            throw new IllegalArgumentException("Cannot unlock achievement for null achievement");
        }
        userService.unlockAchievement(user, achievement);

        logger.log(Level.FINE, "User {0} unlocked achievement {1}", new Object[] {user, achievement});

        communicationService.sendNotification(user, getBase64AchievementImage(achievement),
                ACHIEVEMENT_UNLOCKED_NOTIFICATION_TITLE, achievement.getName());
    }

    private String getBase64AchievementImage(final Achievement achievement) {
        return resourceHelper.getBase64Image("static/img/achievement/" + achievement.getImage());
    }

    @Override
    public void unlockAchievement(final UserIdHolder user, final String achievementName) {
        Achievement achievement = achievementRepository.findByName(achievementName);
        unlockAchievement(user, achievement);
    }

    @Override
    public List<AchievementStatusInfo> getAchievementStatusesForUser(final UserIdHolder idHolder) {
        if (idHolder == null) {
            throw new IllegalArgumentException("Cannot get achievement status for null user");
        }

        List<Achievement> allAchievements = achievementRepository.findAll();

        List<Achievement> userUnlockedAchievements = userService.getUnlockedAchievements(idHolder);

        List<AchievementProgress> achievementProgresses = achievementProgressRepository.findByUser(
                userRepository.findOne(idHolder.getId()));

        List<AchievementStatusInfo> achievementStatusInfos = new ArrayList<>(allAchievements.size());
        for (Achievement achievement : allAchievements) {
            AchievementStatusInfo info = new AchievementStatusInfo();
            info.setAchievement(achievement);

            if (userUnlockedAchievements.contains(achievement)) {
                info.setUnlocked(true);
            } else {
                for (AchievementProgress progress : achievementProgresses) {
                    if (achievement.equals(progress.getAchievement())) {
                        info.setProgress(progress.getProgress());
                        break;
                    }
                }
            }
            achievementStatusInfos.add(info);
        }

        return achievementStatusInfos;
    }

}
