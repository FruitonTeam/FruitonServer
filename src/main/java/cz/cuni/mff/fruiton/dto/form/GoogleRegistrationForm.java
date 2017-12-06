package cz.cuni.mff.fruiton.dto.form;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Pattern;

public final class GoogleRegistrationForm {

    @NotBlank
    private String idToken;

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
