package cz.cuni.mff.fruiton.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import cz.cuni.mff.fruiton.dao.UserRepository;
import cz.cuni.mff.fruiton.dao.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class AuthenticationService {

    private static final Logger logger = Logger.getLogger(AuthenticationService.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GoogleIdTokenVerifier verifier;

    @Autowired
    private PasswordService passwdService;

    public User authenticate(String login, String password) {
        User user = userRepository.findByLogin(login);
        if (user == null) {
            throw new UsernameNotFoundException("User: " + login);
        }

        boolean authenticated;
        try {
            authenticated = passwdService.isPasswordEqual(password, user.getPasswordSalt(), user.getPasswordHash());
        } catch (InvalidKeySpecException e) {
            logger.log(Level.WARNING, "Cannot compare passwords for user: {0}", login);
            throw new AuthenticationServiceException("Cannot complete authentication, please write a ticket");
        }

        if (!authenticated) {
            throw new BadCredentialsException("Incorrect password");
        }

        return user;
    }

    public GoogleIdToken.Payload authenticate(String idTokenStr) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenStr);

            if (idToken != null) {
                return idToken.getPayload();
            }

        } catch (GeneralSecurityException e) {
            // probably ignore, google provides no documentation when this exception occurs
        } catch (IOException e) {
            logger.log(Level.WARNING, "IOException occurred while verifying google token", e);
        }

        throw new AuthenticationServiceException("Could not verify token");
    }

}
