package cz.cuni.mff.fruiton.service.authentication;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import cz.cuni.mff.fruiton.dao.model.User;

public interface AuthenticationService {

    User authenticate(String login, String password);

    GoogleIdToken.Payload authenticate(String idTokenStr);

}
