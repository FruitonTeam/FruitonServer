package cz.cuni.mff.fruiton.controller.api;

import com.mongodb.DuplicateKeyException;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.UUID;
import java.util.stream.Collectors;

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
        return generateTokenForUser(user);
    }

    private String generateTokenForUser(final User user) {
        String userToken = generateToken();
        tokenService.register(userToken, user);
        return userToken;
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
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

    @ExceptionHandler(ConstraintViolationException.class)
    public final ResponseEntity<String> handleConstraintValidationException(final ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream().map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public final ResponseEntity<String> handleDuplicateKeyException(final DuplicateKeyException e) {
        return new ResponseEntity<>(e.getErrorMessage(), HttpStatus.CONFLICT);
    }

    @RequestMapping(value = "/api/loginGoogle")
    public final GoogleLoginResult loginGoogle(@RequestParam final String idToken) {
        User user = authService.authenticate(idToken);
        String token = generateTokenForUser(user);
        return new GoogleLoginResult(user.getLogin(), token);
    }

    private static class GoogleLoginResult {

        private String login;
        private String token;

        private GoogleLoginResult(final String login, final String token) {
            this.login = login;
            this.token = token;
        }

        public String getLogin() {
            return login;
        }

        public String getToken() {
            return token;
        }

        public void setLogin(final String login) {
            this.login = login;
        }

        public void setToken(final String token) {
            this.token = token;
        }
    }

}
