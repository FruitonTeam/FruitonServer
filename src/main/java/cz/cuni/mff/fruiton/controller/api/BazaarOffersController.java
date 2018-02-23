package cz.cuni.mff.fruiton.controller.api;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.BazaarProtos.TradeOffer;
import cz.cuni.mff.fruiton.dto.BazaarProtos.TradeOfferList;
import cz.cuni.mff.fruiton.service.authentication.AuthenticationService;
import cz.cuni.mff.fruiton.service.game.bazaar.BazaarService;
import cz.cuni.mff.fruiton.web.MediaTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public final class BazaarOffersController {

    private static final Logger logger = Logger.getLogger(BazaarOffersController.class.getName());

    private final AuthenticationService authService;

    private final BazaarService bazaarService;

    @Autowired
    public BazaarOffersController(final AuthenticationService authService, final BazaarService bazaarService) {
        this.authService = authService;
        this.bazaarService = bazaarService;
    }

    @GetMapping(value = "/api/secured/bazaar/getTradeOffers", produces = MediaTypes.PROTOBOUF)
    public TradeOfferList getTradeOffers() {
        UserIdHolder user = authService.getLoggedInUser();

        TradeOfferList.Builder offersBuilder = TradeOfferList.newBuilder();

        bazaarService.getOffersOfferedTo(user).forEach(offer -> {
            offersBuilder.addTradeOffers(TradeOffer.newBuilder()
                    .setOfferId(offer.getId())
                    .setOfferedFrom(offer.getOfferedBy().getLogin())
                    .setOfferedTo(offer.getOfferedTo().getLogin())
                    .setFruitonId(offer.getFruitonId())
                    .setPrice(offer.getPrice())
            );
        });

        return offersBuilder.build();
    }

    @GetMapping("/api/secured/bazaar/provideResultForTradeOffer")
    public void provideResultForTradeOffer(@RequestParam final String offerId, @RequestParam final boolean accepted) {
        UserIdHolder from = authService.getLoggedInUser();

        logger.log(Level.FINEST, "Received offer result from {0} for {1} with resolution: {2}",
                new Object[]{from, offerId, accepted});

        if (accepted) {
            bazaarService.buy(offerId, from, false);
        } else {
            bazaarService.removeOfferByOfferedTo(offerId, from);
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAuthenticationServiceException(final Exception e) {
        return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
