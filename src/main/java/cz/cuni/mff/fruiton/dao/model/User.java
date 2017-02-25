package cz.cuni.mff.fruiton.dao.model;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

@Document
public class User {

    private static final int LOGIN_MIN_LENGTH = 4;

    @Id
    private String id;

    @Indexed(unique = true)
    @Length(min = LOGIN_MIN_LENGTH)
    private String login;

    @NotBlank
    private String passwordHash;

    @NotBlank
    private String passwordSalt;

    @Email
    @NotBlank
    private String email;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public User withLogin(String login) {
        setLogin(login);
        return this;
    }

    public User withPasswordHash(String passwordHash) {
        setPasswordHash(passwordHash);
        return this;
    }

    public User withPasswordSalt(String passwordSalt) {
        setPasswordSalt(passwordSalt);
        return this;
    }

    public User withEmail(String email) {
        setEmail(email);
        return this;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", login='" + login + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
