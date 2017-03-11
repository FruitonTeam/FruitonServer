package cz.cuni.mff.fruiton.service.authentication;

import cz.cuni.mff.fruiton.dto.UserProtos;

public interface RegistrationService {

    class RegistrationException extends RuntimeException {

        public RegistrationException(String message) {
            super(message);
        }

    }

    class MailConfirmationNotFound extends RegistrationException {

        public MailConfirmationNotFound(String message) {
            super(message);
        }

    }

    void register(UserProtos.RegistrationData data);

    void confirmEmail(String confirmationId);

}
