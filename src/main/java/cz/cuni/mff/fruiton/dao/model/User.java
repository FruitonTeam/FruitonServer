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

    private static final int PW_MIN_LENGTH = 6;

    private static final int SALT_SIZE = 20;

    private static final int HASH_ITERATION_COUNT = 13;

    private static final int KEY_LENGTH = 64;

    private static final String HASH_ALGORITHM = "PBKDF2WithHmacSHA1";

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

    public void setPassword(@Length(min = PW_MIN_LENGTH) String password) throws InvalidKeySpecException {
        byte[] salt = getSalt();
        passwordSalt = Base64.getEncoder().encodeToString(salt);
        passwordHash = Base64.getEncoder().encodeToString(getHash(password, salt));
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isPasswordEqual(String password) throws InvalidKeySpecException {
        byte[] hash = getHash(password, Base64.getDecoder().decode(passwordSalt));
        return passwordHash.equals(Base64.getEncoder().encodeToString(hash));
    }

    public User withLogin(String login) {
        setLogin(login);
        return this;
    }

    public User withPassword(String password) throws InvalidKeySpecException {
        setPassword(password);
        return this;
    }

    public User withEmail(String email) {
        setEmail(email);
        return this;
    }

    private byte[] getSalt() {
        byte[] salt = new byte[SALT_SIZE];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        return salt;
    }

    private byte[] getHash(String password, byte[] salt) throws InvalidKeySpecException {

        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, HASH_ITERATION_COUNT, KEY_LENGTH);

        SecretKeyFactory factory;
        try {
            factory = SecretKeyFactory.getInstance(HASH_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError(e);
        }

        return factory.generateSecret(spec).getEncoded();
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
