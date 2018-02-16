package cz.cuni.mff.fruiton.service.util.impl;

import cz.cuni.mff.fruiton.test.util.TestUtils;
import cz.cuni.mff.fruiton.util.StorageUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@WebAppConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest(classes = cz.cuni.mff.fruiton.Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ImageServiceImplTest {

    private static final String DEFAULT_AVATAR_NAME = "my_avatar.png";

    @Autowired
    private ImageServiceImpl imageService;

    @Test
    public void saveAndRemoveAvatar() throws IOException {
        String avatarImageName = TestUtils.getUniqueAvatarName(DEFAULT_AVATAR_NAME);

        MultipartFile avatar = TestUtils.getDefaultAvatar(avatarImageName);
        imageService.saveAvatar(avatar);
        assertTrue("Avatar was not saved", new File(StorageUtils.getImageRoot(), avatarImageName).exists());

        imageService.removeAvatar(avatarImageName);
        assertFalse("Avatar was not removed", new File(StorageUtils.getImageRoot(), avatarImageName).exists());
    }

    @Test(expected = IllegalArgumentException.class)
    public void saveNullAvatar() throws IOException {
        imageService.saveAvatar((MultipartFile) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeAvatarForNullUser() {
        imageService.removeAvatar((String) null);
    }

}