package cz.cuni.mff.fruiton.controller.api;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import cz.cuni.mff.fruiton.dao.model.User;
import cz.cuni.mff.fruiton.dto.UserProtos;
import cz.cuni.mff.fruiton.service.authentication.AuthenticationService;
import cz.cuni.mff.fruiton.service.authentication.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class LoginController {

    private final AuthenticationService authService;
    private final TokenService tokenService;

    @Autowired
    public LoginController(AuthenticationService authService, TokenService tokenService) {
        this.authService = authService;
        this.tokenService = tokenService;
    }

    @RequestMapping(value = "/api/login", method = RequestMethod.POST)
    public String login(@RequestBody UserProtos.LoginData data) {

        User user = authService.authenticate(data.getLogin(), data.getPassword());
        String userToken = UUID.randomUUID().toString();

        tokenService.register(userToken, user);

        return userToken;
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<String> handleUsernameNotFoundException(UsernameNotFoundException e) {
        return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentialsException(BadCredentialsException e) {
        return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationServiceException.class)
    public ResponseEntity<String> handleAuthenticationServiceException(AuthenticationServiceException e) {
        return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @RequestMapping(value = "/api/loginGoogle", method = RequestMethod.POST)
    public String loginGoogle(@RequestBody UserProtos.LoginGoogle data) {
        GoogleIdToken.Payload payload = authService.authenticate(data.getToken());
        return payload.getSubject();
    }

}
