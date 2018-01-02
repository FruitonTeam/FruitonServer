package cz.cuni.mff.fruiton.controller.web;

import cz.cuni.mff.fruiton.service.authentication.AuthenticationService;
import cz.cuni.mff.fruiton.service.game.FruitonService;
import cz.cuni.mff.fruiton.service.social.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public final class CollectionController {

    private final FruitonService fruitonService;

    private final UserService userService;

    private final AuthenticationService authService;

    @Autowired
    public CollectionController(
            final FruitonService fruitonService,
            final UserService userService,
            final AuthenticationService authService
    ) {
        this.fruitonService = fruitonService;
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping("/collection")
    public String collection(final Model model) {
        model.addAttribute("fruitons", fruitonService.getFruitonInfos(
                userService.getAvailableFruitons(authService.getLoggedInUser())));

        return "collection";
    }

    @GetMapping("/collection/fruiton")
    public String fruitonDetail(final Model model, @RequestParam("id") final int fruitonId) {
        FruitonService.FruitonInfo info = fruitonService.getFruitonInfo(fruitonId);

        model.addAttribute("fruiton", info);

        return "fruitonDetail";
    }

}
