package cz.cuni.mff.fruiton.controller.api;

import com.mongodb.DuplicateKeyException;
import cz.cuni.mff.fruiton.dto.UserProtos;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.service.authentication.RegistrationService;
import cz.cuni.mff.fruiton.service.authentication.impl.RegistrationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;

@RestController
public class RegistrationController {

    private final UserRepository userRepository;

    private final RegistrationService service;

    @Autowired
    public RegistrationController(final UserRepository repository, final RegistrationService service) {
        userRepository = repository;
        this.service = service;
    }

    @RequestMapping(value = "/api/register", method = RequestMethod.POST)
    public final String register(@RequestBody final UserProtos.RegistrationData data) {
        service.register(data);
        return "OK";
    }

    @RequestMapping(value = "/api/confirmMail", method = RequestMethod.GET)
    public final String confirmMail(@RequestParam(value = "confirmationId") final String confirmationId) {
        service.confirmEmail(confirmationId);
        return "Mail confirmed";
    }

    @RequestMapping(value = "/api/getAllRegistered", method = RequestMethod.GET)
    public final List<User> getAllRegistered() {
        return userRepository.findAll();
    }

    @ExceptionHandler(RegistrationServiceImpl.RegistrationException.class)
    public final ResponseEntity<String> handleRegistrationException(final RegistrationServiceImpl.RegistrationException e) {
        return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public final ResponseEntity<String> handleConstraintValidationException(final ConstraintViolationException e) {
        final StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (ConstraintViolation cv : e.getConstraintViolations()) {
            if (first) {
                first = false;
            } else {
                sb.append(' ');
            }
            sb.append(cv.getMessage());
        }
        return new ResponseEntity<>(sb.toString(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public final ResponseEntity<String> handleDuplicateKeyException(final DuplicateKeyException e) {
        return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RegistrationServiceImpl.MailConfirmationNotFound.class)
    public final ResponseEntity<String> handleMailConfirmationNotFoundException(
            final RegistrationServiceImpl.MailConfirmationNotFound e
    ) {
        return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
    }

}
