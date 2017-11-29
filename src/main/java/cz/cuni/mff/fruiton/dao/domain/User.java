package cz.cuni.mff.fruiton.dao.domain;

import cz.cuni.mff.fruiton.service.game.matchmaking.impl.EloRatingServiceImpl;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.validation.constraints.Pattern;
import java.security.Principal;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Document
public final class User implements Principal, UserDetails {

    public static final int PASSWORD_MIN_LENGTH = 6;

    private static final int LOGIN_MIN_LENGTH = 4;

    private static final String DEFAULT_AVATAR = "boy.png";

    private static final String ROLE_USER = "ROLE_USER";

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

    @Transient
    private State state = State.MENU;

    private List<Integer> unlockedFruitons = new LinkedList<>();

    private List<FruitonTeam> teams = new LinkedList<>();

    private String googleSubject;

    @DBRef
    private List<Achievement> unlockedAchievements = new LinkedList<>();

    @DBRef
    private List<Quest> assignedQuests = new LinkedList<>();

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

    @Override
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
        if (avatar == null || avatar.isEmpty()) {
            return DEFAULT_AVATAR;
        }
        return avatar;
    }

    public void setAvatar(final String avatar) {
        this.avatar = avatar;
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

    public void setMoney(final int money) {
        this.money = money;
    }

    public State getState() {
        return state;
    }

    public void setState(final State state) {
        this.state = state;
    }

    public List<Integer> getUnlockedFruitons() {
        return unlockedFruitons;
    }

    public void setUnlockedFruitons(final List<Integer> unlockedFruitons) {
        this.unlockedFruitons = unlockedFruitons;
    }

    public List<FruitonTeam> getTeams() {
        return teams;
    }

    public void setTeams(final List<FruitonTeam> teams) {
        this.teams = teams;
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

    public void setUnlockedAchievements(final List<Achievement> unlockedAchievements) {
        this.unlockedAchievements = unlockedAchievements;
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
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getGrantedAuthorities(getRoles());
    }

    private List<GrantedAuthority> getGrantedAuthorities(final Collection<String> roles) {
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    private List<String> getRoles() {
        return List.of(ROLE_USER);
    }

    @Transient
    @Override
    public String getUsername() {
        return login;
    }

    @Transient
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Transient
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Transient
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Transient
    @Override
    public boolean isEnabled() {
        return true;
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

    @Override
    @Transient
    public String getName() {
        return login;
    }

    public enum State {
        MENU, MATCHMAKING, IN_GAME
    }

}
