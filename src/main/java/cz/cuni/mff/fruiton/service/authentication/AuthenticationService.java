package cz.cuni.mff.fruiton.service.authentication;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import cz.cuni.mff.fruiton.dao.UserIdHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public interface AuthenticationService {

    /**
     * Authenticates user by provided username and password.
     * @param login username
     * @param password password text
     * @return basic user information
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException if no user with name {@code login} exists
     * @throws org.springframework.security.authentication.BadCredentialsException if {@code password} does not match
     * password saved in database
     */
    UserIdHolder authenticate(String login, String password);

    /**
     * Tries to authenticate user by Google token.
     * @param idToken token issued by Google
     * @return basic user information if log in was successful. Otherwise empty.
     */
    Optional<UserIdHolder> authenticate(String idToken);

    /**
     * Assigns {@code user} information to {@code request}'s session.
     * @param user user who should be affiliated with {@code request}
     * @param request http request issued by user
     */
    void createAuthenticatedSession(UserIdHolder user, HttpServletRequest request);

    /**
     * Verifies the authenticity of token provided by user by Google services and returns Google's data about that user.
     * @param idTokenStr token provided by user for Google authentication
     * @return information about user with whom is the {@code idTokenStr} associated
     * @throws org.springframework.security.authentication.AuthenticationServiceException if could not verify token
     */
    GoogleIdToken.Payload verify(String idTokenStr);

    /**
     * Determines which user is currently logged in and returns his info.
     * @return currently logged in user
     */
    UserIdHolder getLoggedInUser();

}
