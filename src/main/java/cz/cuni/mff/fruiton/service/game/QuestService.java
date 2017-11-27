package cz.cuni.mff.fruiton.service.game;

import cz.cuni.mff.fruiton.dao.domain.Quest;
import cz.cuni.mff.fruiton.dao.domain.User;

public interface QuestService {

    void assignNewQuests(User user);

    void updateProgress(User user, Quest quest, int incrementValue);

    void completeQuest(User user, Quest quest);

    void completeQuest(User user, String questname);

}
