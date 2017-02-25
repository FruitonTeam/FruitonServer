package cz.cuni.mff.fruiton.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class LoginConfig {

    private static final String CLIENT_ID = "827606142557-f63cu712orq80s6do9n6aa8s3eu3h7ag.apps.googleusercontent.com";

    @Bean
    public GoogleIdTokenVerifier googleIdTokenVerifier() {
        return new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                .setAudience(Collections.singleton(CLIENT_ID))
                .setIssuer("accounts.google.com")
                .build();
    }

}
