package cz.cuni.mff.fruiton.service.authentication;

import cz.cuni.mff.fruiton.dao.UserIdHolder;

public interface TokenService {

    String register(UserIdHolder user);

    void unregister(UserIdHolder user);

    UserIdHolder getUser(String token);

    boolean isValid(String token);

    void prolongLease(String token);

}
