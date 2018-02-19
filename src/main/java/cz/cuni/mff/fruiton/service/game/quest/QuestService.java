package cz.cuni.mff.fruiton.service.game.quest;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dao.domain.Quest;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dto.GameProtos;

import java.util.List;

public interface QuestService {

    void assignNewQuests(User user);

    void updateProgress(UserIdHolder user, Quest quest, int incrementValue);

    void completeQuest(UserIdHolder user, Quest quest);

    void completeQuest(UserIdHolder user, String questname);

    List<GameProtos.Quest> getAllQuests(UserIdHolder user);

}
