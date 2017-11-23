package cz.cuni.mff.fruiton.component;

import cz.cuni.mff.fruiton.dao.domain.Achievement;
import cz.cuni.mff.fruiton.dao.repository.AchievementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public final class AchievementHelper {

    private static final String[] MOVE_ACTION_ACHIEVEMENTS = {"Sprinter", "Runner", "Marathon man"};

    private static Set<Achievement> moveActionAchievements;

    private final AchievementRepository repository;

    @Autowired
    public AchievementHelper(final AchievementRepository repository) {
        this.repository = repository;
    }

    public Set<Achievement> getMoveActionAchievements() {
        if (moveActionAchievements == null) {
            synchronized (this) {
                if (moveActionAchievements == null) {
                    Set<Achievement> achievements = new HashSet<>();
                    for (String name : MOVE_ACTION_ACHIEVEMENTS) {
                        achievements.add(repository.findByName(name));
                    }
                    moveActionAchievements = Collections.unmodifiableSet(achievements);
                }
            }
        }
        return moveActionAchievements;
    }

}
