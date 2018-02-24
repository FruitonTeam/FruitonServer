package cz.cuni.mff.fruiton.service.social;

import cz.cuni.mff.fruiton.dao.domain.User;

public interface EmailConfirmationService {

    class MailConfirmationNotFound extends RuntimeException {

        public MailConfirmationNotFound(final String message) {
            super(message);
        }

    }

    /**
     * Sends confirm email request to specified user.
     * @param user user to whom send confirm email request
     */
    void sendEmailConfirmationRequest(User user);

    /**
     * Confirms email address.
     * @param confirmationId id of the email confirmation
     */
    void confirmEmail(String confirmationId);

}
