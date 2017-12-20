package cz.cuni.mff.fruiton.service.authentication;

import cz.cuni.mff.fruiton.dao.domain.User;

public interface TokenService {

    String register(User user);

    void unregister(User user);

    User getUser(String token);

    boolean isValid(String token);

    void prolongLease(String token);

}
