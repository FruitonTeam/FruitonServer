package cz.cuni.mff.fruiton.service;

import cz.cuni.mff.fruiton.dao.UserRepository;
import cz.cuni.mff.fruiton.dao.model.User;
import cz.cuni.mff.fruiton.dto.UserProtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class RegistrationService {

    private static final Logger logger = Logger.getLogger(RegistrationService.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordService passwdService;

    public void register(UserProtos.RegistrationData data) {

        PasswordService.Hash passwdHash;
        try {
            passwdHash = passwdService.getPasswordHash(data.getPassword());
        } catch (InvalidKeySpecException e) {
            logger.log(Level.SEVERE, "Could not create password hash", e);
            throw new RegistrationException("Cannot register user because of the internal error");
        }

        User user = new User()
                .withLogin(data.getLogin())
                .withPasswordHash(passwdHash.getHash())
                .withPasswordSalt(passwdHash.getSalt())
                .withEmail(data.getEmail());

        userRepository.save(user);
        logger.log(Level.FINE, "Registered user: {0}", user);
    }

    public static class RegistrationException extends RuntimeException {

        public RegistrationException(String message) {
            super(message);
        }

    }

}
