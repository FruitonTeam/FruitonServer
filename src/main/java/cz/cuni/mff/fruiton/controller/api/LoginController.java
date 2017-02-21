package cz.cuni.mff.fruiton.controller.api;

import cz.cuni.mff.fruiton.dao.UserRepository;
import cz.cuni.mff.fruiton.dao.model.User;
import cz.cuni.mff.fruiton.dto.UserProtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.spec.InvalidKeySpecException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class LoginController {

    private static final Logger logger = Logger.getLogger(LoginController.class.getName());

    @Autowired
    private UserRepository userRepository;

    @RequestMapping(value = "/api/login", method = RequestMethod.POST)
    public String login(@RequestBody UserProtos.Login data) {

        User user = userRepository.findByLogin(data.getLogin());
        if (user == null) {
            throw new UsernameNotFoundException("User: " + data.getLogin());
        }

        boolean authenticated;
        try {
            authenticated = user.isPasswordEqual(data.getPassword());
        } catch (InvalidKeySpecException e) {
            logger.log(Level.WARNING, "Cannot compare passwords for user: {0}", data.getLogin());
            throw new AuthenticationServiceException("Cannot complete authentication, please write a ticket");
        }

        if (!authenticated) {
            throw new BadCredentialsException("Incorrect password");
        }

        return UUID.randomUUID().toString();
    }

}
