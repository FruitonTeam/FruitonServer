package cz.cuni.mff.fruiton.controller.api;

import cz.cuni.mff.fruiton.dto.UserProtos;
import cz.cuni.mff.fruiton.dao.UserRepository;
import cz.cuni.mff.fruiton.dao.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class RegistrationController {

    private static final Logger logger = Logger.getLogger(RegistrationController.class.getName());

    @Autowired
    private UserRepository userRepository;

    @RequestMapping(value = "/api/register", method = RequestMethod.POST)
    public String register(@RequestBody UserProtos.User data) {

        User user;
        try {
            user = new User()
                    .withLogin(data.getLogin())
                    .withPassword(data.getPassword())
                    .withEmail(data.getEmail());
        } catch (InvalidKeySpecException e) {
            logger.log(Level.WARNING, "Cannot create hash for " + data.getPassword(), e);
            throw new IllegalStateException("Cannot fulfill the request because of the internal error");
        }

        userRepository.save(user);
        logger.log(Level.FINE, "Registered user: {0}", user);

        return "OK";
    }

    @RequestMapping(value = "/api/getAllRegistered", method = RequestMethod.GET)
    public List<User> getAllRegistered() {
        return userRepository.findAll();
    }

}
