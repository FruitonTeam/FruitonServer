package cz.cuni.mff.fruiton.dao.domain;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Pattern;
import java.security.Principal;
import java.util.List;

@Document
public class User implements Principal {

    private static final int LOGIN_MIN_LENGTH = 4;

    private static final String DEFAULT_AVATAR = "boy.png";

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

    @DBRef
    private List<Fruiton> salad;

    /** Avatar's image filename. */
    private String avatar;

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

    public final List<Fruiton> getSalad() {
        return salad;
    }

    public final void setSalad(final List<Fruiton> salad) {
        this.salad = salad;
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
}
