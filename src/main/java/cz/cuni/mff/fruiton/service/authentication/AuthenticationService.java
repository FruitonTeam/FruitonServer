package cz.cuni.mff.fruiton.service.authentication;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import cz.cuni.mff.fruiton.dao.UserIdHolder;

import javax.servlet.http.HttpServletRequest;

public interface AuthenticationService {

    UserIdHolder authenticate(String login, String password);

    UserIdHolder authenticate(String idToken);

    void createAuthenticatedSession(UserIdHolder user, HttpServletRequest request);

    GoogleIdToken.Payload verify(String idTokenStr);

    UserIdHolder getLoggedInUser();

}
