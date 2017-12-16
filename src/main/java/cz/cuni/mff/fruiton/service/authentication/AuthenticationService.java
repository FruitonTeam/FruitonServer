package cz.cuni.mff.fruiton.service.authentication;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import cz.cuni.mff.fruiton.dao.domain.User;

import javax.servlet.http.HttpServletRequest;

public interface AuthenticationService {

    User authenticate(String login, String password);

    User authenticate(String idToken);

    void createAuthenticatedSession(User user, HttpServletRequest request);

    GoogleIdToken.Payload verify(String idTokenStr);

    User getLoggedInUser();

}
