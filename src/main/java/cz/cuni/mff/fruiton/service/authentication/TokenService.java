package cz.cuni.mff.fruiton.service.authentication;

import cz.cuni.mff.fruiton.dao.UserIdHolder;

public interface TokenService {

    /**
     * Generates new token for {@code user}.
     * @param user user for whom to generate new token
     * @return generated token
     */
    String register(UserIdHolder user);

    /**
     * Looks up user by token.
     * @param token token by which to look up user
     * @return user for whom the token was generated or null if token is no longer valid
     */
    UserIdHolder getUser(String token);

    /**
     * Determines whether {@code token} is still valid.
     * @param token user's token
     * @return true if valid, false otherwise
     */
    boolean isValid(String token);

}
