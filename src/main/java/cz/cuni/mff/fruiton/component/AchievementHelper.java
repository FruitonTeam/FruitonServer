package cz.cuni.mff.fruiton.component;

import cz.cuni.mff.fruiton.dao.domain.Achievement;
import cz.cuni.mff.fruiton.dao.repository.AchievementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public final class AchievementHelper {

    private static final String[] MOVE_ACTION_ACHIEVEMENTS = {"Sprinter", "Runner", "Marathon man"};
    private static final String[] WIN_GAME_ACHIEVEMENTS = {"Novice", "Apprentice", "Master"};
    private static final String[] KILL_FRUITON_ACHIEVEMENTS = {"Killer", "Serial killer", "Destroyer"};

    private Set<Achievement> moveActionAchievements;
    private Set<Achievement> winGameAchievements;
    private Set<Achievement> killFruitonAchievements;

    private final AchievementRepository repository;

    @Autowired
    public AchievementHelper(final AchievementRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    private void init() {
        moveActionAchievements = getAchievements(MOVE_ACTION_ACHIEVEMENTS);
        winGameAchievements = getAchievements(WIN_GAME_ACHIEVEMENTS);
        killFruitonAchievements = getAchievements(KILL_FRUITON_ACHIEVEMENTS);
    }

    private Set<Achievement> getAchievements(final String[] achievementNames) {
        Set<Achievement> achievements = new HashSet<>();
        for (String name : achievementNames) {
            achievements.add(repository.findByName(name));
        }
        return Collections.unmodifiableSet(achievements);
    }

    public Set<Achievement> getMoveActionAchievements() {
        return moveActionAchievements;
    }

    public Set<Achievement> getWinGameAchievements() {
        return winGameAchievements;
    }

    public Set<Achievement> getKillFruitonAchievements() {
        return killFruitonAchievements;
    }
}
