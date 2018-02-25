package cz.cuni.mff.fruiton.service.game.quest;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dao.domain.Quest;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dto.GameProtos;

import java.util.List;

public interface QuestService {

    /**
     * Assigns new quests for the specified user.
     * @param user user for whom to assign new quests
     */
    void assignNewQuests(User user);

    /**
     * Updates progress of the specified quest.
     * @param user user for whom the quest progress will be updated
     * @param quest quest of the user which will be affected by this update
     * @param incrementValue value by which the progress will be incremented
     */
    void updateProgress(UserIdHolder user, Quest quest, int incrementValue);

    /**
     * Completes specified quest.
     * @param user user who completed the specified quest
     * @param quest completed quest
     */
    void completeQuest(UserIdHolder user, Quest quest);

    /**
     * Completes specified quest.
     * @param user user who completed the specified quest
     * @param questName completed quest name
     */
    void completeQuest(UserIdHolder user, String questName);

    /**
     * Returns all quests assigned to specified user.
     * @param user user for whom to fetch assigned quests
     * @retur nall quests assigned to specified user
     */
    List<GameProtos.Quest> getAllQuests(UserIdHolder user);

}
