package cz.cuni.mff.fruiton.controller.web;

import cz.cuni.mff.fruiton.dao.domain.Message;
import cz.cuni.mff.fruiton.dao.repository.MessageRepository;
import cz.cuni.mff.fruiton.service.authentication.AuthenticationService;
import cz.cuni.mff.fruiton.service.social.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Profile("debug")
@Controller
public final class DebugController {

    private static final int FRUITON_TO_UNLOCK = 6;

    private static final int MONEY_CHANGE_VALUE = 100;

    private final UserService userService;

    private final MessageRepository messageRepository;

    private final AuthenticationService authService;

    @Autowired
    public DebugController(
            final MessageRepository messageRepository,
            final UserService userService,
            final AuthenticationService authService
    ) {
        this.messageRepository = messageRepository;
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping("/debug/unlockFruiton")
    public String unlockFruiton() {
        userService.unlockFruiton(authService.getLoggedInUser(), FRUITON_TO_UNLOCK);

        return "redirect:/home";
    }

    @GetMapping("/debug/addMoney")
    public String addMoney() {
        userService.adjustMoney(authService.getLoggedInUser(), MONEY_CHANGE_VALUE);

        return "redirect:/home";
    }

    @RequestMapping(value = "/api/debug/getAllMessages", method = RequestMethod.GET)
    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

}
