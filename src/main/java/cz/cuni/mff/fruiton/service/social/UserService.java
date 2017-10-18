package cz.cuni.mff.fruiton.service.social;

import cz.cuni.mff.fruiton.dao.domain.FruitonTeam;
import cz.cuni.mff.fruiton.dao.domain.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    void changeAvatar(User user, MultipartFile avatar);

    void changePassword(User user, String newPassword);

    void changeEmail(User user, String newEmail);

    User findUserByLogin(String login);

    User findUser(String id);

    void addTeam(User user, FruitonTeam fruitonTeam);

    void removeTeam(User user, String teamName);

}
