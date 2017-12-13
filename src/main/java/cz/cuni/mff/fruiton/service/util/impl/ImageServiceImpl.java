package cz.cuni.mff.fruiton.service.util.impl;

import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.service.util.ImageService;
import cz.cuni.mff.fruiton.util.StorageUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public final class ImageServiceImpl implements ImageService {

    private static final String IMAGE_CONTENT_TYPE_PREFIX = "image/";

    private static final Logger logger = Logger.getLogger(ImageServiceImpl.class.getName());

    @Override
    public String saveAvatar(final MultipartFile avatar) throws IOException {
        if (avatar == null) {
            throw new IllegalArgumentException("Cannot save null avatar");
        }
        if (avatar.getContentType() == null || !avatar.getContentType().startsWith(IMAGE_CONTENT_TYPE_PREFIX)) {
            throw new IllegalArgumentException("Avatar must be an image");
        }

        File imgRoot = StorageUtils.getImageRoot();

        File imageFile = getUniqueFile(imgRoot, avatar.getOriginalFilename());
        avatar.transferTo(imageFile);

        return imageFile.getName();
    }

    private File getUniqueFile(final File dir, final String fileName) {
        File file = new File(dir, fileName);
        while (file.exists()) {
            String uuid = UUID.randomUUID().toString();
            file = new File(dir, uuid + fileName);
        }
        return file;
    }

    @Override
    public String saveAvatar(final String url) throws IOException {
        File imgRoot = StorageUtils.getImageRoot();

        String imageName = FilenameUtils.getName(url);
        File imageFile = getUniqueFile(imgRoot, imageName);

        FileUtils.copyURLToFile(new URL(url), imageFile);

        return imageFile.getName();
    }

    @Override
    public void removeAvatar(final User user) {
        if (user == null) {
            throw new IllegalArgumentException("Cannot remove avatar for null user");
        }

        if (!user.isAvatarSet()) {
            throw new IllegalArgumentException("Cannot delete empty avatar for user " + user);
        }

        removeAvatar(user.getAvatar());
    }

    @Override
    public void removeAvatar(final String avatarImageName) {
        if (avatarImageName == null) {
            throw new IllegalArgumentException("Cannot remove avatar for null image name");
        }

        File imageFile = new File(StorageUtils.getImageRoot(), avatarImageName);
        try {
            deleteFile(imageFile);
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "Inconsistency, user has avatar set but it does not exist on filesystem", e);
        }
    }

    private void deleteFile(final File file) throws FileNotFoundException {
        if (!file.exists()) {
            throw new FileNotFoundException("Could not find file " + file.getName());
        }

        logger.log(Level.FINEST, "Removing file {0}", file);

        if (!file.delete()) {
            throw new IllegalStateException("Could not delete file " + file.getName());
        }
    }

    @Override
    public String getBase64Avatar(final User user) throws IOException {
        return Base64.getEncoder().encodeToString(Files.readAllBytes(
                Paths.get(StorageUtils.IMAGE_PATH, user.getAvatar())));
    }

}
