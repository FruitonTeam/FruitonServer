package cz.cuni.mff.fruiton.controller.api;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dto.UserProtos;
import cz.cuni.mff.fruiton.service.authentication.AuthenticationService;
import cz.cuni.mff.fruiton.service.authentication.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class LoginController {

    private final AuthenticationService authService;
    private final TokenService tokenService;

    @Autowired
    public LoginController(final AuthenticationService authService, final TokenService tokenService) {
        this.authService = authService;
        this.tokenService = tokenService;
    }

    @RequestMapping(value = "/api/login", method = RequestMethod.POST)
    public final String login(@RequestBody final UserProtos.LoginData data) {

        User user = authService.authenticate(data.getLogin(), data.getPassword());
        String userToken = UUID.randomUUID().toString();

        tokenService.register(userToken, user);

        return userToken;
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public final ResponseEntity<String> handleUsernameNotFoundException(final UsernameNotFoundException e) {
        return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public final ResponseEntity<String> handleBadCredentialsException(final BadCredentialsException e) {
        return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationServiceException.class)
    public final ResponseEntity<String> handleAuthenticationServiceException(final AuthenticationServiceException e) {
        return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @RequestMapping(value = "/api/loginGoogle", method = RequestMethod.POST)
    public final String loginGoogle(@RequestBody final UserProtos.LoginGoogle data) {
        GoogleIdToken.Payload payload = authService.authenticate(data.getToken());
        return payload.getSubject();
    }

}
