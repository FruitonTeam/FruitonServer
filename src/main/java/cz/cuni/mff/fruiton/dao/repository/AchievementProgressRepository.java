package cz.cuni.mff.fruiton.dao.repository;

import cz.cuni.mff.fruiton.dao.domain.Achievement;
import cz.cuni.mff.fruiton.dao.domain.AchievementProgress;
import cz.cuni.mff.fruiton.dao.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AchievementProgressRepository extends MongoRepository<AchievementProgress, String> {

    AchievementProgress findByUserAndAchievement(User user, Achievement achievement);

    List<AchievementProgress> findByUser(User user);

}
