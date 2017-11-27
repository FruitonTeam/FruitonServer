package cz.cuni.mff.fruiton.service.game.impl;

import cz.cuni.mff.fruiton.dao.domain.Quest;
import cz.cuni.mff.fruiton.dao.domain.QuestProgress;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dao.repository.QuestProgressRepository;
import cz.cuni.mff.fruiton.dao.repository.QuestRepository;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.service.communication.CommunicationService;
import cz.cuni.mff.fruiton.service.game.QuestService;
import cz.cuni.mff.fruiton.util.ResourceUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public final class QuestServiceImpl implements QuestService {

    private static final String QUEST_COMPLETED_NOTIFICATION_TITLE = "Quest completed";

    private static final int MAX_QUEST_NUMBER = 1; // TODO: change to 2/3 when more quests will be available

    private static final Logger logger = Logger.getLogger(QuestServiceImpl.class.getName());

    private final UserRepository userRepository;

    private final QuestRepository questRepository;
    private final QuestProgressRepository questProgressRepository;

    private final CommunicationService communicationService;

    public QuestServiceImpl(
            final UserRepository userRepository,
            final QuestRepository questRepository,
            final QuestProgressRepository questProgressRepository,
            final CommunicationService communicationService
    ) {
        this.userRepository = userRepository;
        this.questProgressRepository = questProgressRepository;
        this.questRepository = questRepository;
        this.communicationService = communicationService;
    }

    @Override
    public void assignNewQuests(final User user) {
        if (user.getAssignedQuests() == null || user.getAssignedQuests().size() < MAX_QUEST_NUMBER) {
            // TODO: change when we will have more quests available
            user.setAssignedQuests(List.of(questRepository.findByName("Winner")));
            userRepository.save(user);
        }
    }

    @Override
    public void updateProgress(final User user, final Quest quest, final int incrementValue) {
        if (user == null) {
            throw new IllegalArgumentException("Cannot update quest progress for null user");
        }
        if (quest == null) {
            throw new IllegalArgumentException("Cannot update quest progress for null quest");
        }
        if (!user.getAssignedQuests().contains(quest)) {
            return;
        }
        if (incrementValue < 0) {
            throw new IllegalArgumentException("Cannot decrease quest progress");
        }
        if (incrementValue == 0) {
            return;
        }

        QuestProgress questProgress = questProgressRepository.findByUserAndQuest(user, quest);
        if (questProgress == null) {
            questProgress = new QuestProgress(user, quest);
        }

        questProgress.incrementProgress(incrementValue);

        if (questProgress.isCompleted()) {
            if (questProgress.getId() != null) { // progress is stored in db, we need to delete it
                questProgressRepository.delete(questProgress);
            }
            completeQuest(user, quest);
        } else {
            questProgressRepository.save(questProgress);
        }

        communicationService.sendNotification(user, getBase64QuestImage(quest),
                QUEST_COMPLETED_NOTIFICATION_TITLE, quest.getName());
    }

    private String getBase64QuestImage(final Quest quest) {
        return ResourceUtils.getBase64Image("static/img/quest/" + quest.getImage());
    }

    @Override
    public void completeQuest(final User user, final Quest quest) {
        if (user == null) {
            throw new IllegalArgumentException("Cannot complete quest for null user");
        }
        if (!user.getAssignedQuests().contains(quest)) {
            return;
        }

        logger.log(Level.FINE, "User {0} completed quest {1}", new Object[] {user, quest});

        user.adjustMoney(quest.getReward().getMoney());
        user.getAssignedQuests().remove(quest);

        assignNewQuests(user);
    }

    @Override
    public void completeQuest(final User user, final String questName) {
        completeQuest(user, questRepository.findByName(questName));
    }

}
