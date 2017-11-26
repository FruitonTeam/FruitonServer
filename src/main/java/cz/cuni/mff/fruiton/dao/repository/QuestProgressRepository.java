package cz.cuni.mff.fruiton.dao.repository;

import cz.cuni.mff.fruiton.dao.domain.Quest;
import cz.cuni.mff.fruiton.dao.domain.QuestProgress;
import cz.cuni.mff.fruiton.dao.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QuestProgressRepository extends MongoRepository<QuestProgress, String> {

    QuestProgress findByUserAndQuest(User user, Quest quest);

}
