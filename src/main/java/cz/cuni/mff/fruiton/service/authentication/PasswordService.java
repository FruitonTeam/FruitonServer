package cz.cuni.mff.fruiton.service.authentication;

/**
 * Provides basic password functionality.
 */
public interface PasswordService {

    /**
     * Renews password for specified user.
     * @param email email of the user we want to renew password for
     */
    void renew(String email);

}
