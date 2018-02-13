package cz.cuni.mff.fruiton.service.authentication;

import cz.cuni.mff.fruiton.dao.UserIdHolder;

public interface TokenService {

    String register(UserIdHolder user);

    UserIdHolder getUser(String token);

    boolean isValid(String token);

}
