package cz.cuni.mff.fruiton.service.util;

import cz.cuni.mff.fruiton.dao.domain.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageService {

    /**
     * Saves specified image.
     * @param file file to save
     * @return name of the saved file
     * @throws IOException if could not save provided file
     */
    String saveAvatar(MultipartFile file) throws IOException;

    /**
     * Downloads image from the specified url and saves it.
     * @param url url from which to download the image.
     * @return name of the saved file
     * @throws IOException if could not save provided file
     */
    String saveAvatar(String url) throws IOException;

    /**
     * Removes avatar image for specified user.
     * @param user user for whom to remove the avatar
     */
    void removeAvatar(User user);

    /**
     * Removes specified avatar.
     * @param avatarImageName name of the avatar's image
     */
    void removeAvatar(String avatarImageName);

    /**
     * Returns base64 representation of specified avatar.
     * @param avatar name of the avatar image
     * @return base64 representation of specified avatar
     * @throws IOException if could not load avatar file
     */
    String getBase64Avatar(String avatar) throws IOException;

}
