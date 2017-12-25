package cz.cuni.mff.fruiton.dto.form;

import cz.cuni.mff.fruiton.dao.domain.User;
import org.hibernate.validator.constraints.Email;

import javax.validation.constraints.Size;

public final class RenewPasswordForm {

    @Size(max = User.EMAIL_MAX_LENGTH)
    @Email
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }
}
