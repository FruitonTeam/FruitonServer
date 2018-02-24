package cz.cuni.mff.fruiton.service.authentication;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.UserProtos.RegistrationData;
import cz.cuni.mff.fruiton.dto.form.RegistrationForm;

public interface RegistrationService {

    class RegistrationException extends RuntimeException {

        public RegistrationException(final String message) {
            super(message);
        }

    }

    /**
     * Registers new user.
     * @param data data by which to register user
     * @throws RegistrationException if could not register user
     */
    void register(RegistrationData data);

    /**
     * Registers new user.
     * @param registrationForm data by which to register user
     * @return new registered user's info
     */
    UserIdHolder register(RegistrationForm registrationForm);

    /**
     * Registers new user.
     * @param login login of the new user
     * @param payload Google information about the new user
     * @return new registered user's info
     */
    UserIdHolder register(String login, GoogleIdToken.Payload payload);

}
