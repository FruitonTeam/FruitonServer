package cz.cuni.mff.fruiton.dto.form;

import cz.cuni.mff.fruiton.dao.domain.User;
import org.hibernate.validator.constraints.Email;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public final class RegistrationForm {

    @Pattern(regexp = "^[_A-z0-9]{4,}$")
    private String login;

    @Size(min = User.PASSWORD_MIN_LENGTH)
    private String password;

    @Email
    private String email;

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
}
