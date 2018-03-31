package cz.cuni.mff.fruiton.dao.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Document
public final class Achievement implements Comparable<Achievement> {

    @Id
    private String id;

    @NotBlank
    @Indexed(unique = true)
    private String name;

    @NotBlank
    private String description;

    @NotBlank
    private String image;

    @Min(value = 0, message = "Achievement goal cannot be negative")
    private int goal = 0;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(final String image) {
        this.image = image;
    }

    public int getGoal() {
        return goal;
    }

    public void setGoal(final int goal) {
        this.goal = goal;
    }

    @Transient
    public boolean isProgressAchievement() {
        return goal != 0;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Achievement that = (Achievement) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public int compareTo(final Achievement o) {
        return this.name.compareTo(o.name);
    }
}
