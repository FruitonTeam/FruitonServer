package cz.cuni.mff.fruiton.controller.web;

import cz.cuni.mff.fruiton.component.TokenAuthenticationFilter;
import cz.cuni.mff.fruiton.dto.form.AddBazaarOfferForm;
import cz.cuni.mff.fruiton.service.authentication.AuthenticationService;
import cz.cuni.mff.fruiton.service.game.BazaarService;
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

    @Autowired
    public BazaarController(
            final BazaarService bazaarService,
            final UserService userService,
            final AuthenticationService authService
    ) {
        this.bazaarService = bazaarService;
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping("/bazaar")
    public String bazaar(
            final Model model,
            final @RequestParam(value = TokenAuthenticationFilter.AUTH_TOKEN_KEY, required = false) String token
    ) {
        if (token != null) { // if user clicked on Market button in client app then strip the token header
            return "redirect:/bazaar";
        }
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
        model.addAttribute("offers", bazaarService.getOffersForUser(authService.getLoggedInUser()));

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
        bazaarService.buy(offerId, authService.getLoggedInUser());

        return "redirect:/home";
    }

}
