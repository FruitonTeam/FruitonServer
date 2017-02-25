package cz.cuni.mff.fruiton.controller.api;

import cz.cuni.mff.fruiton.dto.UserProtos;
import cz.cuni.mff.fruiton.dao.UserRepository;
import cz.cuni.mff.fruiton.dao.model.User;
import cz.cuni.mff.fruiton.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    @RequestMapping(value = "/api/getAllRegistered", method = RequestMethod.GET)
    public List<User> getAllRegistered() {
        return userRepository.findAll();
    }

}
