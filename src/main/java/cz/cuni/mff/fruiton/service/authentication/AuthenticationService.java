package cz.cuni.mff.fruiton.service.authentication;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import cz.cuni.mff.fruiton.dao.UserIdHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public interface AuthenticationService {

    UserIdHolder authenticate(String login, String password);

    Optional<UserIdHolder> authenticate(String idToken);

    void createAuthenticatedSession(UserIdHolder user, HttpServletRequest request);

    GoogleIdToken.Payload verify(String idTokenStr);

    UserIdHolder getLoggedInUser();

}
