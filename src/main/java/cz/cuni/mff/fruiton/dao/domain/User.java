package cz.cuni.mff.fruiton.dao.domain;

import cz.cuni.mff.fruiton.component.util.UserInfoCache;
import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.service.game.matchmaking.impl.EloRatingServiceImpl;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Document
public final class User {

    public static final int LOGIN_MAX_LENGTH = 50;

    public static final int PASSWORD_MIN_LENGTH = 6;

    public static final int PASSWORD_MAX_LENGTH = 50;

    public static final int EMAIL_MAX_LENGTH = 50;

    private static final int LOGIN_MIN_LENGTH = 4;

    private static final String AVATAR_PATH = "/avatar/";

    private static final String DEFAULT_AVATAR = "/img/boy.png";

    @Id
    private String id;

    @Indexed(unique = true)
    @Length(min = LOGIN_MIN_LENGTH, message = "Login has to have at least 4 characters.")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Login can contain only alphanumeric characters.")
    private String login;

    @NotBlank
    private String password;

    @NotBlank
    @Email(message = "Invalid email address.")
    @Indexed(unique = true)
    private String email;

    private boolean emailConfirmed = false;

    /** Avatar's image filename. */
    private String avatar;

    private int rating = EloRatingServiceImpl.DEFAULT_RATING;

    private int money = 0;

    private List<Integer> unlockedFruitons = new LinkedList<>();

    private List<FruitonTeam> teams = new LinkedList<>();

    private String googleSubject;

    @DBRef
    private List<Achievement> unlockedAchievements = new LinkedList<>();

    @DBRef
    private List<Quest> assignedQuests = new LinkedList<>();

    @DBRef(lazy = true) // lazy fetching so we won't get stack overflow exception
    private Set<User> friends = new HashSet<>();

    private LocalDate dateOfLastCompletedQuest;

    private GameProtos.Fraction fraction = GameProtos.Fraction.NONE;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(final String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public boolean isEmailConfirmed() {
        return emailConfirmed;
    }

    public void setEmailConfirmed(final boolean emailConfirmed) {
        this.emailConfirmed = emailConfirmed;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(final String avatar) {
        this.avatar = avatar;
        UserInfoCache.invalidate(UserIdHolder.of(this));
    }

    public int getRating() {
        return rating;
    }

    public void setRating(final int rating) {
        this.rating = rating;
    }

    public int getMoney() {
        return money;
    }

    public List<Integer> getUnlockedFruitons() {
        return new ArrayList<>(unlockedFruitons); // make a copy
    }

    public List<FruitonTeam> getTeams() {
        return teams;
    }

    public String getGoogleSubject() {
        return googleSubject;
    }

    public void setGoogleSubject(final String googleSubject) {
        this.googleSubject = googleSubject;
    }

    public List<Achievement> getUnlockedAchievements() {
        return unlockedAchievements;
    }

    public List<Quest> getAssignedQuests() {
        return assignedQuests;
    }

    public void setAssignedQuests(final List<Quest> assignedQuests) {
        this.assignedQuests = assignedQuests;
    }

    public boolean isAvatarSet() {
        return avatar != null && !avatar.isEmpty();
    }

    public void adjustMoney(final int value) {
        money += value;
        UserInfoCache.invalidate(UserIdHolder.of(this));
    }

    public void unlockFruiton(final int fruitonId) {
        unlockedFruitons.add(fruitonId);
    }

    public void unlockFruitons(final Collection<Integer> fruitonIds) {
        unlockedFruitons.addAll(fruitonIds);
    }

    public void removeFruitonFromUnlockedFruitons(final int fruitonId) {
        unlockedFruitons.remove(Integer.valueOf(fruitonId));
    }

    public String getAvatarWebImageMapping() {
        if (isAvatarSet()) {
            return AVATAR_PATH + avatar;
        } else {
            return DEFAULT_AVATAR;
        }
    }

    public void addFriend(final User user) {
        friends.add(user);
    }

    public void removeFriend(final User user) {
        friends.remove(user);
    }

    public Set<User> getFriends() {
        return new HashSet<>(friends); // return a copy
    }

    public boolean canGenerateNewQuest() {
        return assignedQuests.isEmpty() && !LocalDate.now().equals(dateOfLastCompletedQuest);
    }

    public void setQuestCompletedToday() {
        this.dateOfLastCompletedQuest = LocalDate.now();
    }

    public GameProtos.Fraction getFraction() {
        return fraction;
    }

    public void setFraction(final GameProtos.Fraction f) {
        if (f == GameProtos.Fraction.NONE) {
            throw new IllegalArgumentException("Cannot set fraction to NONE");
        } else if (this.fraction == GameProtos.Fraction.NONE) {
            this.fraction = f;
        } else {
            throw new IllegalStateException("Fraction has already been chosen");
        }
    }

    public User withLogin(final String login) {
        setLogin(login);
        return this;
    }

    public User withPassword(final String password) {
        setPassword(password);
        return this;
    }

    public User withEmail(final String email) {
        setEmail(email);
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        User user = (User) o;

        return login != null ? login.equals(user.login) : user.login == null;
    }

    @Override
    public int hashCode() {
        return login != null ? login.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "User{"
                + "id='" + id + '\''
                + ", login='" + login + '\''
                + ", email='" + email + '\''
                + '}';
    }

}
