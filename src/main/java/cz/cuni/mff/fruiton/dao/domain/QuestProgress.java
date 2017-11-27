package cz.cuni.mff.fruiton.dao.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Min;

@Document
public final class QuestProgress {

    @Id
    private String id;

    @DBRef
    private User user;

    @DBRef
    private Quest quest;

    @Min(value = 0, message = "Quest progress cannot be negative")
    private int progress = 0;

    public QuestProgress(final User user, final Quest quest) {
        this.user = user;
        this.quest = quest;
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

    public Quest getQuest() {
        return quest;
    }

    public void setQuest(final Quest quest) {
        this.quest = quest;
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
        return progress >= quest.getGoal();
    }

}
