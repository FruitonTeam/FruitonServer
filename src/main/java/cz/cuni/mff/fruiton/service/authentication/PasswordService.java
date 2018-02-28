package cz.cuni.mff.fruiton.service.authentication;

/**
 * Provides basic password functionality.
 */
public interface PasswordService {

    /**
     * Resets password for specified user.
     * @param email email of the user we want to reset password for
     */
    void reset(String email);

}
