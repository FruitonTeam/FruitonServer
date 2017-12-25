package cz.cuni.mff.fruiton.controller.web;

import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.service.authentication.AuthenticationService;
import cz.cuni.mff.fruiton.service.game.FruitonService;
import cz.cuni.mff.fruiton.service.game.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public final class CollectionController {

    private final AuthenticationService authService;

    private final FruitonService fruitonService;

    private final PlayerService playerService;

    @Autowired
    public CollectionController(
            final AuthenticationService authService,
            final FruitonService fruitonService,
            final PlayerService playerService
    ) {
        this.authService = authService;
        this.fruitonService = fruitonService;
        this.playerService = playerService;
    }

    @GetMapping("/collection")
    public String collection(final Model model) {
        User user = authService.getLoggedInUser();

        model.addAttribute("fruitons", fruitonService.getFruitonInfos(playerService.getAvailableFruitons(user)));

        return "collection";
    }

    @GetMapping("/collection/fruiton")
    public String fruitonDetail(final Model model, @RequestParam("id") final int fruitonId) {
        FruitonService.FruitonInfo info = fruitonService.getFruitonInfo(fruitonId);

        model.addAttribute("fruiton", info);

        return "fruitonDetail";
    }

}
