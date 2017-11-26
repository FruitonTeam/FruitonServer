package cz.cuni.mff.fruiton.dao.repository;

import cz.cuni.mff.fruiton.dao.domain.Quest;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QuestRepository extends MongoRepository<Quest, String> {

    Quest findByName(String name);

}
