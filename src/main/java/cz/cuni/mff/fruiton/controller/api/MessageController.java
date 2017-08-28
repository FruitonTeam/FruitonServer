package cz.cuni.mff.fruiton.controller.api;

import cz.cuni.mff.fruiton.dao.domain.Message;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dao.repository.MessageRepository;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MessageController {

    private final MessageRepository repository;
    private final UserRepository userRepository;

    @Autowired
    public MessageController(final MessageRepository repository, final UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @RequestMapping(value = "/api/debug/getAllMessages", method = RequestMethod.GET)
    public final List<Message> getAllMessages() {
        return repository.findAll();
    }

    @RequestMapping(value = "/api/debug/getAllMessagesFor", method = RequestMethod.GET)
    public final List<Message> getAllMessagesFor(@RequestParam final String login) {
        User user = userRepository.findByLogin(login);
        if (user == null) {
            throw new UsernameNotFoundException("Could not find user: " + login);
        }

        return repository.findAllFor(user.getId());
    }

}
