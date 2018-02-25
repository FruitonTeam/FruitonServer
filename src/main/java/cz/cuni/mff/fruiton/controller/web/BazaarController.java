package cz.cuni.mff.fruiton.controller.web;

import cz.cuni.mff.fruiton.dto.form.AddBazaarOfferForm;
import cz.cuni.mff.fruiton.service.authentication.AuthenticationService;
import cz.cuni.mff.fruiton.service.game.bazaar.BazaarService;
import cz.cuni.mff.fruiton.service.game.FruitonService;
import cz.cuni.mff.fruiton.service.social.UserService;
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

    private final BazaarService bazaarService;

    private final UserService userService;

    private final AuthenticationService authService;

    private final FruitonService fruitonService;

    @Autowired
    public BazaarController(
            final BazaarService bazaarService,
            final UserService userService,
            final AuthenticationService authService,
            final FruitonService fruitonService
    ) {
        this.bazaarService = bazaarService;
        this.userService = userService;
        this.authService = authService;
        this.fruitonService = fruitonService;
    }

    @GetMapping("/bazaar")
    public String bazaar(final Model model) {
        model.addAttribute("bestOffers", bazaarService.getBestOffers());

        return "bazaar/index";
    }

    @GetMapping("/bazaar/{id}")
    public String bazzarItem(final Model model, @PathVariable("id") final int id) {
        if (!fruitonService.exists(id)) {
            throw new IllegalArgumentException("No fruiton with id " + id + " exists");
        }

        Fruiton f = KernelUtils.getFruiton(id);

        model.addAttribute("fruitonName", f.name);
        model.addAttribute("offers", bazaarService.getOrderedOffersForFruiton(id));

        return "bazaar/bazaarItem";
    }

    @GetMapping("/bazaar/addOffer")
    public String addBazaarOfferForm(final Model model) {
        model.addAttribute("fruitons", userService.getFruitonsAvailableForSelling(authService.getLoggedInUser()));
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
        model.addAttribute("offers", bazaarService.getOffersCreatedBy(authService.getLoggedInUser()));

        return "bazaar/myOffers";
    }

    @GetMapping("/bazaar/removeOffer")
    public String removeOffer(
            @RequestParam final String offerId,
            @RequestHeader(value = "referer", required = false) final String referer
    ) {
        bazaarService.removeOffer(offerId, authService.getLoggedInUser());

        if (referer != null) {
            return "redirect:" + referer;
        }
        return "redirect:/home";
    }

    @GetMapping("/bazaar/buy")
    public String buy(@RequestParam final String offerId) {
        bazaarService.buy(offerId, authService.getLoggedInUser(), true);

        return "redirect:/home";
    }

}
