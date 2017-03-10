package cz.cuni.mff.fruiton.controller.api;

import com.mongodb.DuplicateKeyException;
import cz.cuni.mff.fruiton.dto.UserProtos;
import cz.cuni.mff.fruiton.dao.UserRepository;
import cz.cuni.mff.fruiton.dao.model.User;
import cz.cuni.mff.fruiton.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;

@RestController
public class RegistrationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RegistrationService service;

    @RequestMapping(value = "/api/register", method = RequestMethod.POST)
    public String register(@RequestBody UserProtos.RegistrationData data) {

        service.register(data);

        return "OK";
    }

    @ExceptionHandler(RegistrationService.RegistrationException.class)
    public ResponseEntity<String> handleRegistrationException(RegistrationService.RegistrationException e) {
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

    @RequestMapping(value = "/api/getAllRegistered", method = RequestMethod.GET)
    public List<User> getAllRegistered() {
        return userRepository.findAll();
    }

}
