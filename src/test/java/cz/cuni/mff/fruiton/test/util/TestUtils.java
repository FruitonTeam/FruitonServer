package cz.cuni.mff.fruiton.test.util;

import cz.cuni.mff.fruiton.dto.UserProtos.RegistrationData;

public class TestUtils {

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

}
