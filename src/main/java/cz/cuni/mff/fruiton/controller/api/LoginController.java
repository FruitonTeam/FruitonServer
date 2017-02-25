package cz.cuni.mff.fruiton.controller.api;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import cz.cuni.mff.fruiton.dao.model.User;
import cz.cuni.mff.fruiton.dto.UserProtos;
import cz.cuni.mff.fruiton.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class LoginController {

    @Autowired
    AuthenticationService authService;

    @RequestMapping(value = "/api/login", method = RequestMethod.POST)
    public String login(@RequestBody UserProtos.LoginData data) {

        User user = authService.authenticate(data.getLogin(), data.getPassword());

        return UUID.randomUUID().toString();
    }

    @RequestMapping(value = "/api/loginGoogle", method = RequestMethod.POST)
    public String loginGoogle(@RequestBody String idTokenStr) {
        GoogleIdToken.Payload payload = authService.authenticate(idTokenStr);
        return payload.getSubject();
    }

}
