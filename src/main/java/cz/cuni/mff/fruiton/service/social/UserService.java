package cz.cuni.mff.fruiton.service.social;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dto.GameProtos;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    void changeAvatar(User user, MultipartFile avatar);

    void changeAvatar(User user, String url);

    void changePassword(User user, String newPassword);

    void changeEmail(User user, String newEmail);

    User findUserByLogin(String login);

    User findUser(String id);

    GameProtos.LoggedPlayerInfo getLoggedPlayerInfo(User user);

    String generateRandomName(GoogleIdToken.Payload payload);

}
