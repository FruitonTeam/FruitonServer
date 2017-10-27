package cz.cuni.mff.fruiton.service.authentication;

import cz.cuni.mff.fruiton.dao.domain.User;

public interface AuthenticationService {

    User authenticate(String login, String password);

    User authenticate(String idToken);

}
