package cz.cuni.mff.fruiton.service.authentication;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.UserProtos;
import cz.cuni.mff.fruiton.dto.form.RegistrationForm;

public interface RegistrationService {

    class RegistrationException extends RuntimeException {

        public RegistrationException(final String message) {
            super(message);
        }

    }

    void register(UserProtos.RegistrationData data);

    UserIdHolder register(RegistrationForm registrationForm);

    UserIdHolder register(String login, GoogleIdToken.Payload payload);

}
