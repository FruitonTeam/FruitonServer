package cz.cuni.mff.fruiton.service.util;

import cz.cuni.mff.fruiton.dao.domain.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageService {

    String saveAvatar(MultipartFile file) throws IOException;

    String saveAvatar(String url) throws IOException;

    void removeAvatar(User user);

    void removeAvatar(String avatarImageName);

    String getBase64Avatar(String avatar) throws IOException;

}
