package cz.cuni.mff.fruiton.controller.web;

import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.service.authentication.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Profile("debug")
@Controller
public final class DebugController {

    private static final int FRUITON_TO_UNLOCK = 6;

    private static final int MONEY_CHANGE_VALUE = 100;

    private final UserRepository repository;

    private final AuthenticationService authService;

    @Autowired
    public DebugController(final UserRepository repository, final AuthenticationService authService) {
        this.repository = repository;
        this.authService = authService;
    }

    @GetMapping("/debug/unlockFruiton")
    public String unlockFruiton() {
        User user = authService.getLoggedInUser();

        user.unlockFruiton(FRUITON_TO_UNLOCK);
        repository.save(user);

        return "redirect:/home";
    }

    @GetMapping("/debug/addMoney")
    public String addMoney() {
        User user = authService.getLoggedInUser();

        user.adjustMoney(MONEY_CHANGE_VALUE);
        repository.save(user);

        return "redirect:/home";
    }

}
