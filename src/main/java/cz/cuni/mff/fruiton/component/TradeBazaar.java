package cz.cuni.mff.fruiton.component;

import cz.cuni.mff.fruiton.annotation.ProtobufMessage;
import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.BazaarProtos.TradeOffer;
import cz.cuni.mff.fruiton.dto.CommonProtos.ErrorMessage.ErrorId;
import cz.cuni.mff.fruiton.dto.CommonProtos.WrapperMessage;
import cz.cuni.mff.fruiton.exception.FruitonServerException;
import cz.cuni.mff.fruiton.service.communication.CommunicationService;
import cz.cuni.mff.fruiton.service.game.bazaar.BazaarService;
import cz.cuni.mff.fruiton.service.social.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

import static cz.cuni.mff.fruiton.dto.CommonProtos.WrapperMessage.MessageCase;

@Component
public final class TradeBazaar {

    private static class TradeException extends FruitonServerException {

        TradeException(final String message) {
            super(message, ErrorId.TRADE_OFFER_ERROR);
        }

    }

    private static final Logger logger = Logger.getLogger(TradeBazaar.class.getName());

    private final CommunicationService communicationService;

    private final UserService userService;

    private final BazaarService bazaarService;

    @Autowired
    public TradeBazaar(
            final CommunicationService communicationService,
            final UserService userService,
            final BazaarService bazaarService
    ) {
        this.communicationService = communicationService;
        this.userService = userService;
        this.bazaarService = bazaarService;
    }

    @ProtobufMessage(messageCase = MessageCase.TRADEOFFER)
    private void createZeroFeeOffer(final UserIdHolder from, final TradeOffer offer) {
        if (offer.getPrice() < 0) {
            throw new TradeException("Cannot sell fruiton for negative price");
        }

        UserIdHolder offerFor = userService.tryFindUserByLogin(offer.getOfferedTo());
        if (offerFor == null) {
            throw new TradeException("Could not offer fruiton to unknown user: " + offer.getOfferedTo());
        }

        if (from.equals(offerFor)) {
            throw new TradeException("User cannot offer fruiton to himself");
        }

        logger.log(Level.FINER, "Creating trade offer from {0} for {1}: (fruiton: {2}, price: {3})",
                new Object[]{from, offerFor, offer.getFruitonId(), offer.getPrice()});

        String offerId = bazaarService.createOffer(from, offerFor, offer.getFruitonId(), offer.getPrice());

        WrapperMessage msg = WrapperMessage.newBuilder()
                    .setTradeOffer(TradeOffer.newBuilder(offer)
                            .setOfferId(offerId)
                            .setOfferedFrom(from.getUsername()))
                    .build();
        communicationService.send(offerFor, msg);
    }

}
