package cz.cuni.mff.fruiton.test.util;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.dto.CommonProtos;
import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.dto.UserProtos;
import cz.cuni.mff.fruiton.dto.UserProtos.RegistrationData;
import cz.cuni.mff.fruiton.service.authentication.RegistrationService;
import cz.cuni.mff.fruiton.util.KernelUtils;
import cz.cuni.mff.fruiton.util.StorageUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public class TestUtils {

    public static final String DEFAULT_LOGIN = "login";
    public static final String DEFAULT_PASSWORD = "password";

    private static final String DEFAULT_EMAIL = "test@test.com";

    private static final String DEFAULT_AVATAR_IMAGE_PATH = "/static/img/boy.png";
    private static final String PNG_CONTENT_TYPE = "image/png";

    private TestUtils() {

    }

    public static RegistrationData getRegistrationData(
            final String mail,
            final String login,
            final String password
    ) {
        return RegistrationData.newBuilder()
                .setEmail(mail)
                .setLogin(login)
                .setPassword(password)
                .build();
    }

    public static RegistrationData getDefaultRegistrationData() {
        return getRegistrationData(DEFAULT_EMAIL, DEFAULT_LOGIN, DEFAULT_PASSWORD);
    }

    public static User defaultRegister(final RegistrationService registrationService, final UserRepository userRepository) {
        UserProtos.RegistrationData data = getDefaultRegistrationData();
        registrationService.register(data);
        return userRepository.findByLogin(TestUtils.DEFAULT_LOGIN);
    }

    public static MultipartFile getDefaultAvatar(String avatarImageName) throws IOException {
        InputStream is = TestUtils.class.getResourceAsStream(DEFAULT_AVATAR_IMAGE_PATH);
        MultipartFile avatar = new MockMultipartFile(avatarImageName, avatarImageName, PNG_CONTENT_TYPE, is);
        is.close();

        return avatar;
    }

    public static String getUniqueAvatarName(String defaultAvatarName) {
        String avatarImageName = defaultAvatarName;
        while (new File(StorageUtils.getImageRoot(), avatarImageName).exists()) {
            avatarImageName = UUID.randomUUID().toString() + avatarImageName;
        }
        return avatarImageName;
    }

    public static String login(final String login, final String password, final int port) {
        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.CONTENT_TYPE, List.of("application/json"));

        String requestBody = "{\"login\": \"" + login +"\", \"password\": \"" + password + "\"}";

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = new TestRestTemplate()
                .postForEntity("http://localhost:" + port + "/api/login", request, String.class);

        return response.getBody();
    }

    public static GameProtos.FruitonTeam getDefaultFruitonTeam() {

        return GameProtos.FruitonTeam.newBuilder()
                .setName("test_team_name")
                .addAllFruitonIDs(List.of(1, 4, 4, 4, 4, 10, 10, 10, 10, 10))
                .addAllPositions(List.of(
                        KernelUtils.positionOf(0, 0),
                        KernelUtils.positionOf(1, 0),
                        KernelUtils.positionOf(2, 0),
                        KernelUtils.positionOf(3, 0),
                        KernelUtils.positionOf(4, 0),
                        KernelUtils.positionOf(0, 1),
                        KernelUtils.positionOf(1, 1),
                        KernelUtils.positionOf(2, 1),
                        KernelUtils.positionOf(3, 1),
                        KernelUtils.positionOf(4, 1)
                        )
                )
                .build();
    }

    public static GameProtos.FindGame buildFindGameMsg() {
        return GameProtos.FindGame.newBuilder()
                .setTeam(TestUtils.getDefaultFruitonTeam())
                .build();
    }

    public static CommonProtos.WrapperMessage buildFindGameMsgWrapped() {
        return CommonProtos.WrapperMessage.newBuilder()
                .setFindGame(buildFindGameMsg())
                .build();
    }

    public static UserIdHolder createUser(final UserRepository userRepository, final String login, final int rating) {
        User user = new User();
        user.setLogin(login);
        user.setPassword(RandomStringUtils.randomAlphanumeric(User.PASSWORD_MIN_LENGTH));
        user.setRating(rating);
        user.setEmail(RandomStringUtils.randomAlphanumeric(5) + "@" + login + ".com");

        userRepository.save(user);

        return UserIdHolder.of(user);
    }

}
