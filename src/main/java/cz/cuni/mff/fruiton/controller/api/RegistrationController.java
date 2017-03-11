package cz.cuni.mff.fruiton.controller.api;

import com.mongodb.DuplicateKeyException;
import cz.cuni.mff.fruiton.dto.UserProtos;
import cz.cuni.mff.fruiton.dao.UserRepository;
import cz.cuni.mff.fruiton.dao.model.User;
import cz.cuni.mff.fruiton.service.authentication.RegistrationService;
import cz.cuni.mff.fruiton.service.authentication.impl.RegistrationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;

@RestController
public class RegistrationController {

    private final UserRepository userRepository;

    private final RegistrationService service;

    @Autowired
    public RegistrationController(UserRepository repository, RegistrationService service) {
        userRepository = repository;
        this.service = service;
    }

    @RequestMapping(value = "/api/register", method = RequestMethod.POST)
    public String register(@RequestBody UserProtos.RegistrationData data) {
        service.register(data);
        return "OK";
    }

    @RequestMapping(value = "/api/confirmMail", method = RequestMethod.GET)
    public String confirmMail(@RequestParam(value = "confirmationId") String confirmationId) {
        service.confirmEmail(confirmationId);
        return "Mail confirmed";
    }

    @RequestMapping(value = "/api/getAllRegistered", method = RequestMethod.GET)
    public List<User> getAllRegistered() {
        return userRepository.findAll();
    }

    @ExceptionHandler(RegistrationServiceImpl.RegistrationException.class)
    public ResponseEntity<String> handleRegistrationException(RegistrationServiceImpl.RegistrationException e) {
        return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintValidationException(ConstraintViolationException e) {
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
    public ResponseEntity<String> handleDuplicateKeyException(DuplicateKeyException e) {
        return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RegistrationServiceImpl.MailConfirmationNotFound.class)
    public ResponseEntity<String> handleMailConfirmationNotFoundException(RegistrationServiceImpl.MailConfirmationNotFound e) {
        return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
    }

}
