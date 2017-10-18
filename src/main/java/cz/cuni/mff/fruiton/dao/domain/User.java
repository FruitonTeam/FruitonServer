package cz.cuni.mff.fruiton.dao.domain;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
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
public class User implements Principal, UserDetails {

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

    private int rating;

    @Transient
    private State state = State.MENU;

    private List<Integer> unlockedFruitons = new LinkedList<>();

    public final String getId() {
        return id;
    }

    public final void setId(final String id) {
        this.id = id;
    }

    public final String getLogin() {
        return login;
    }

    public final void setLogin(final String login) {
        this.login = login;
    }

    @Override
    public final String getPassword() {
        return password;
    }

    public final void setPassword(final String password) {
        this.password = password;
    }

    public final String getEmail() {
        return email;
    }

    public final void setEmail(final String email) {
        this.email = email;
    }

    public final boolean isEmailConfirmed() {
        return emailConfirmed;
    }

    public final void setEmailConfirmed(final boolean emailConfirmed) {
        this.emailConfirmed = emailConfirmed;
    }

    public final String getAvatar() {
        if (avatar == null || avatar.isEmpty()) {
            return DEFAULT_AVATAR;
        }
        return avatar;
    }

    public final void setAvatar(final String avatar) {
        this.avatar = avatar;
    }

    public final int getRating() {
        return rating;
    }

    public final void setRating(final int rating) {
        this.rating = rating;
    }

    public final State getState() {
        return state;
    }

    public final void setState(final State state) {
        this.state = state;
    }

    public final List<Integer> getUnlockedFruitons() {
        return unlockedFruitons;
    }

    public final void setUnlockedFruitons(final List<Integer> unlockedFruitons) {
        this.unlockedFruitons = unlockedFruitons;
    }

    public final boolean isAvatarSet() {
        return avatar != null && !avatar.isEmpty();
    }

    public final User withLogin(final String login) {
        setLogin(login);
        return this;
    }

    public final User withPassword(final String password) {
        setPassword(password);
        return this;
    }

    public final User withEmail(final String email) {
        setEmail(email);
        return this;
    }

    @Override
    public final Collection<? extends GrantedAuthority> getAuthorities() {
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
    public final String getUsername() {
        return login;
    }

    @Transient
    @Override
    public final boolean isAccountNonExpired() {
        return true;
    }

    @Transient
    @Override
    public final boolean isAccountNonLocked() {
        return true;
    }

    @Transient
    @Override
    public final boolean isCredentialsNonExpired() {
        return true;
    }

    @Transient
    @Override
    public final boolean isEnabled() {
        return true;
    }

    @Override
    public final boolean equals(final Object o) {
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
    public final int hashCode() {
        return login != null ? login.hashCode() : 0;
    }

    @Override
    public final String toString() {
        return "User{"
                + "id='" + id + '\''
                + ", login='" + login + '\''
                + ", email='" + email + '\''
                + '}';
    }

    @Override
    @Transient
    public final String getName() {
        return login;
    }

    public enum State {
        MENU, MATCHMAKING, IN_GAME
    }

}
