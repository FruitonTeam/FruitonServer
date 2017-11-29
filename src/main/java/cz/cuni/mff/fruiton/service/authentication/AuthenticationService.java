package cz.cuni.mff.fruiton.service.authentication;

import cz.cuni.mff.fruiton.dao.domain.User;

import javax.servlet.http.HttpServletRequest;

public interface AuthenticationService {

    User authenticate(String login, String password);

    User authenticate(String idToken);

    void createAuthenticatedSession(User user, HttpServletRequest request);

}
