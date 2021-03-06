package cz.cuni.mff.fruiton.controller.api;

import com.mongodb.DuplicateKeyException;
import cz.cuni.mff.fruiton.dto.UserProtos;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.service.authentication.RegistrationService;
import cz.cuni.mff.fruiton.service.authentication.impl.RegistrationServiceImpl;
import cz.cuni.mff.fruiton.service.social.EmailConfirmationService.MailConfirmationNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

@RestController("apiRegistrationController")
public class RegistrationController {

    private static final String DUPLICATED_LOGIN_MSG = "User with provided login already exists";

    private static final String DUPLICATED_EMAIL_MSG = "Email address is already in use";

    private final UserRepository userRepository;

    private final RegistrationService service;

    @Autowired
    public RegistrationController(final UserRepository userRepository, final RegistrationService service) {
        this.userRepository = userRepository;
        this.service = service;
    }

    @RequestMapping(value = "/api/register", method = RequestMethod.POST)
    public final String register(@RequestBody final UserProtos.RegistrationData data) {
        service.register(data);
        return "OK";
    }

    @RequestMapping(value = "/api/debug/getAllRegistered", method = RequestMethod.GET)
    public final List<User> getAllRegistered() {
        return userRepository.findAll();
    }

    @ExceptionHandler(RegistrationServiceImpl.RegistrationException.class)
    public final ResponseEntity<String> handleRegistrationException(final RegistrationServiceImpl.RegistrationException e) {
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
        return new ResponseEntity<>(getUserFriendlyDuplicateKeyExceptionMessage(e), HttpStatus.BAD_REQUEST);
    }

    private String getUserFriendlyDuplicateKeyExceptionMessage(final DuplicateKeyException e) {
        String defaultMsg = e.getMessage();
        if (defaultMsg.contains("login dup key")) {
            return DUPLICATED_LOGIN_MSG;
        } else if (defaultMsg.contains("email dup key")) {
            return DUPLICATED_EMAIL_MSG;
        } else {
            return defaultMsg;
        }
    }

    @ExceptionHandler(MailConfirmationNotFound.class)
    public final ResponseEntity<String> handleMailConfirmationNotFoundException(
            final MailConfirmationNotFound e
    ) {
        return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
    }

}
