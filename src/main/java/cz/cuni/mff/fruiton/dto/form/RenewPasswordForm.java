package cz.cuni.mff.fruiton.dto.form;

import org.hibernate.validator.constraints.Email;

public final class RenewPasswordForm {

    @Email
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }
}
