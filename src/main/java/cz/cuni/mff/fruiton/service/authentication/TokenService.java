package cz.cuni.mff.fruiton.service.authentication;

import cz.cuni.mff.fruiton.dao.domain.User;

public interface TokenService {

    void register(String token, User user);

    User getUserAndInvalidateToken(String token);

    boolean isValid(String token);

}
