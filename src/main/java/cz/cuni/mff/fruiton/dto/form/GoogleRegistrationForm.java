package cz.cuni.mff.fruiton.dto.form;

import cz.cuni.mff.fruiton.dao.domain.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public final class GoogleRegistrationForm {

    @NotBlank
    private String idToken;

    @Size(max = User.LOGIN_MAX_LENGTH)
    @Pattern(regexp = "^[_A-z0-9]{4,}$")
    private String login;

    public GoogleRegistrationForm() {
    }

    public GoogleRegistrationForm(final String idToken, final String login) {
        this.idToken = idToken;
        this.login = login;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(final String idToken) {
        this.idToken = idToken;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(final String login) {
        this.login = login;
    }

}
