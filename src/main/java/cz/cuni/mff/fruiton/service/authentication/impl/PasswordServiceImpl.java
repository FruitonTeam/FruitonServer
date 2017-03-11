package cz.cuni.mff.fruiton.service.authentication.impl;

import cz.cuni.mff.fruiton.service.authentication.PasswordService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

@Service
@PropertySource("classpath:security.properties")
public class PasswordServiceImpl implements PasswordService {

    @Value("${salt.size}")
    private int saltSize;

    @Value("${hash.iteration.count}")
    private int hashIterationCount;

    @Value("${hash.key.length}")
    private int keyLength;

    @Value("${hash.algorithm}")
    private String hashAlgorithm;

    @Override
    public Hash getPasswordHash(@Nonnull String password) throws InvalidKeySpecException {
        byte[] salt = getSalt();
        String passwordSalt = Base64.getEncoder().encodeToString(salt);
        String passwordHash = Base64.getEncoder().encodeToString(getHash(password, salt));

        return new Hash(passwordSalt, passwordHash);
    }

    @Override
    public boolean isPasswordEqual(@Nonnull String password, @Nonnull String salt, @Nonnull String passwordHash)
            throws InvalidKeySpecException {

        byte[] hash = getHash(password, Base64.getDecoder().decode(salt));
        return passwordHash.equals(Base64.getEncoder().encodeToString(hash));
    }

    private byte[] getSalt() {
        byte[] salt = new byte[saltSize];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        return salt;
    }

    private byte[] getHash(String password, byte[] salt) throws InvalidKeySpecException {

        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, hashIterationCount, keyLength);

        SecretKeyFactory factory;
        try {
            factory = SecretKeyFactory.getInstance(hashAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError(e);
        }

        return factory.generateSecret(spec).getEncoded();
    }

}
