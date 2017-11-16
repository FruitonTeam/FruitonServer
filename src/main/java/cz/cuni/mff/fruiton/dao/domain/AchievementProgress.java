package cz.cuni.mff.fruiton.dao.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Min;

@Document
public final class AchievementProgress {

    @Id
    private String id;

    @DBRef
    private User user;

    @DBRef
    private Achievement achievement;

    @Min(value = 0, message = "Achievement progress cannot be negative")
    private int progress = 0;

    public AchievementProgress(final User user, final Achievement achievement) {
        this.user = user;
        this.achievement = achievement;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public Achievement getAchievement() {
        return achievement;
    }

    public void setAchievement(final Achievement achievement) {
        this.achievement = achievement;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(final int progress) {
        this.progress = progress;
    }

    public void incrementProgress(final int incrementValue) {
        this.progress += incrementValue;
    }

    @Transient
    public boolean isCompleted() {
        return progress >= achievement.getGoal();
    }

}
