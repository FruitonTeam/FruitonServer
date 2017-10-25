package cz.cuni.mff.fruiton.service.social;

import cz.cuni.mff.fruiton.dao.domain.User;

public interface EmailConfirmationService {

    class MailConfirmationNotFound extends RuntimeException {

        public MailConfirmationNotFound(final String message) {
            super(message);
        }

    }

    void sendEmailConfirmationRequest(User user);

    void confirmEmail(String confirmationId);

}
