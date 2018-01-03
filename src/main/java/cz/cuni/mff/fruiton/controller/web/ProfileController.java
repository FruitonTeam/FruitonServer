package cz.cuni.mff.fruiton.controller.web;

import cz.cuni.mff.fruiton.service.social.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ProfileController {

    private final UserService userService;

    @Autowired
    public ProfileController(final UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "/profile/{login}", method = RequestMethod.GET)
    public final String profileInfo(final Model model, @PathVariable("login") final String login) {
        model.addAttribute("playerInfo", userService.getPlayerInfo(login));
        return "profile";
    }

}
