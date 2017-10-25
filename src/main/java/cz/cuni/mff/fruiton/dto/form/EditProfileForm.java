package cz.cuni.mff.fruiton.dto.form;

import cz.cuni.mff.fruiton.dao.domain.User;
import org.hibernate.validator.constraints.Email;
import org.springframework.web.multipart.MultipartFile;

public class EditProfileForm {

    private String password;

    @Email
    private String email;

    private MultipartFile avatar;

    public static EditProfileForm parseFrom(final User user) {
        EditProfileForm form = new EditProfileForm();
        form.email = user.getEmail();
        return form;
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

    public final MultipartFile getAvatar() {
        return avatar;
    }

    public final void setAvatar(final MultipartFile avatar) {
        this.avatar = avatar;
    }

}
