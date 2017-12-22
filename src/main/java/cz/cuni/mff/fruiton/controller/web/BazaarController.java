package cz.cuni.mff.fruiton.controller.web;

import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dto.form.AddBazaarOfferForm;
import cz.cuni.mff.fruiton.service.authentication.AuthenticationService;
import cz.cuni.mff.fruiton.service.game.BazaarService;
import cz.cuni.mff.fruiton.service.game.PlayerService;
import cz.cuni.mff.fruiton.util.KernelUtils;
import fruiton.kernel.Fruiton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Controller
public final class BazaarController {

    private final PlayerService playerService;

    private final BazaarService bazaarService;

    private final AuthenticationService authService;

    @Autowired
    public BazaarController(
            final PlayerService playerService,
            final BazaarService bazaarService,
            final AuthenticationService authService
    ) {
        this.playerService = playerService;
        this.bazaarService = bazaarService;
        this.authService = authService;
    }

    @GetMapping("/bazaar")
    public String bazaar(final Model model) {
        model.addAttribute("bestOffers", bazaarService.getBestOffers());

        return "bazaar/index";
    }

    @GetMapping("/bazaar/{id}")
    public String bazzarItem(final Model model, @PathVariable("id") final int id) {
        Fruiton f = KernelUtils.getFruiton(id); // TODO: check if fruiton exists

        model.addAttribute("fruitonName", f.model);
        model.addAttribute("offers", bazaarService.getOrderedOffersForFruiton(id));

        return "bazaar/bazaarItem";
    }

    @GetMapping("/bazaar/addOffer")
    public String addBazaarOfferForm(final Model model) {
        User user = authService.getLoggedInUser();

        model.addAttribute("fruitons", playerService.getFruitonsAvailableForSelling(user));
        model.addAttribute("formModel", new AddBazaarOfferForm());

        return "bazaar/addOffer";
    }

    @PostMapping("/bazaar/addOffer")
    public String addBazaarOfferFormSubmit(@Valid final AddBazaarOfferForm form) {
        bazaarService.createOffer(authService.getLoggedInUser(), form.getFruitonId(), form.getPrice());

        return "redirect:/bazaar/" + form.getFruitonId();
    }

    @GetMapping("/bazaar/myOffers")
    public String myOffers(final Model model) {
        User user = authService.getLoggedInUser();
        model.addAttribute("offers", bazaarService.getOffersForUser(user));

        return "bazaar/myOffers";
    }

    @GetMapping("/bazaar/removeOffer")
    public String removeOffer(
            @RequestParam final String offerId,
            @RequestHeader(value = "referer", required = false) final String referer
    ) {
        User user = authService.getLoggedInUser();
        bazaarService.removeOffer(offerId, user);

        if (referer != null) {
            return "redirect:" + referer;
        }
        return "redirect:/home";
    }

    @GetMapping("/bazaar/buy")
    public String buy(@RequestParam final String offerId) {
        User user = authService.getLoggedInUser();
        bazaarService.buy(offerId, user);

        return "redirect:/home";
    }

}
