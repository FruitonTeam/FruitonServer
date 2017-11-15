package cz.cuni.mff.fruiton.dao.repository;

import cz.cuni.mff.fruiton.dao.domain.Achievement;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AchievementRepository extends MongoRepository<Achievement, String> {

    Achievement findByName(String name);

}
