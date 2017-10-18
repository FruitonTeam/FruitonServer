package cz.cuni.mff.fruiton.controller.web;

import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LadderController {

    private static final int PAGE_SIZE = 20;

    private final UserRepository userRepository;

    @Autowired
    public LadderController(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @RequestMapping(value = "/ladder", method = RequestMethod.GET)
    public final  String ladder(final Model model, @RequestParam(required = false) final Integer page) {
        int ladderPage;
        if (page != null && page >= 1) {
            ladderPage = page;
        } else {
            ladderPage = 1;
        }

        Page<User> users = userRepository.findAllByOrderByRatingDesc(new PageRequest(ladderPage - 1, PAGE_SIZE));

        model.addAttribute("users", users.getContent());
        model.addAttribute("page", ladderPage);
        model.addAttribute("pages", users.getTotalPages());
        return "ladder";
    }

}
