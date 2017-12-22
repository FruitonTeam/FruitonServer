package cz.cuni.mff.fruiton.service.game.impl;

import cz.cuni.mff.fruiton.dao.domain.BazaarOffer;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dao.repository.BazaarOfferRepository;
import cz.cuni.mff.fruiton.service.game.BazaarService;
import cz.cuni.mff.fruiton.service.social.UserService;
import cz.cuni.mff.fruiton.util.KernelUtils;
import fruiton.kernel.Fruiton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public final class BazaarServiceImpl implements BazaarService {

    private static final Logger logger = Logger.getLogger(BazaarServiceImpl.class.getName());

    private final MongoTemplate mongoTemplate;

    private final BazaarOfferRepository bazaarOfferRepository;

    private final UserService userService;

    @Autowired
    public BazaarServiceImpl(
            final MongoTemplate mongoTemplate,
            final BazaarOfferRepository bazaarOfferRepository,
            final UserService userService
    ) {
        this.mongoTemplate = mongoTemplate;
        this.bazaarOfferRepository = bazaarOfferRepository;
        this.userService = userService;
    }

    @Override
    public List<BazaarOfferListItem> getBestOffers() {
        GroupOperation groupBy = Aggregation.group("fruitonId").min("price").as("minPrice");
        ProjectionOperation projection = Aggregation.project("minPrice").and("fruitonId").previousOperation();

        TypedAggregation<BazaarOffer> aggregation = Aggregation.newAggregation(BazaarOffer.class, groupBy, projection);

        AggregationResults<BestOfferAggrResult> result = mongoTemplate.aggregate(aggregation, BestOfferAggrResult.class);

        List<BazaarOfferListItem> listItems = new ArrayList<>(result.getMappedResults().size());

        for (BestOfferAggrResult res : result) {
            Fruiton f = KernelUtils.getFruiton(res.fruitonId);
            BazaarOfferListItem listItem = new BazaarOfferListItem(f.model, res.minPrice, res.fruitonId);
            listItems.add(listItem);
        }

        return listItems;
    }

    @Override
    public void createOffer(final User user, final int fruitonId, final int price) {
        if (user == null) {
            throw new IllegalArgumentException("Null user cannot create offer");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Offer must have positive price");
        }
        if (!user.getUnlockedFruitons().contains(fruitonId)) {
            throw new IllegalArgumentException("User cannot sell fruiton he does not have");
        }

        BazaarOffer offer = new BazaarOffer(price, user, fruitonId);
        bazaarOfferRepository.save(offer);

        userService.removeFruitonFromUnlockedFruitons(user, fruitonId);

        logger.log(Level.FINER, "Created offer {0}", offer);
    }

    @Override
    public List<BazaarOfferListItemWithId> getOffersForUser(final User user) {
        return bazaarOfferRepository.findByOfferedBy(user).stream().map(offer -> {
            Fruiton f = KernelUtils.getFruiton(offer.getFruitonId());
            return new BazaarOfferListItemWithId(offer.getId(),
                    new BazaarOfferListItem(f.model, offer.getPrice(), offer.getFruitonId()));
        }).collect(Collectors.toList());
    }

    @Override
    public void removeOffer(final String offerId, final User user) {
        BazaarOffer offer = bazaarOfferRepository.findOne(offerId);
        if (offer == null) {
            throw new IllegalArgumentException("No bazaar offer with id " + offerId);
        }

        if (!offer.getOfferedBy().equals(user)) {
            throw new SecurityException("User " + user + " cannot remove offer " + offer
                    + " because this offer was not offered by him");
        }

        userService.unlockFruiton(user, offer.getFruitonId());
        bazaarOfferRepository.delete(offer);
    }

    @Override
    public void buy(final String offerId, final User user) {
        if (user == null) {
            throw new IllegalArgumentException("Null user cannot buy a fruiton");
        }

        BazaarOffer offer = bazaarOfferRepository.findOne(offerId);
        if (offer == null) {
            throw new IllegalArgumentException("No bazaar offer with id " + offerId);
        }

        if (offer.getOfferedBy().equals(user)) {
            throw new IllegalStateException("User cannot buy his own offer");
        }

        if (user.getMoney() < offer.getPrice()) {
            throw new IllegalStateException("User has insufficient money to buy offer " + offer);
        }

        userService.unlockFruiton(user, offer.getFruitonId());
        userService.adjustMoney(user, -offer.getPrice());

        userService.adjustMoney(offer.getOfferedBy(), offer.getPrice());

        bazaarOfferRepository.delete(offer);
    }

    @Override
    public List<BazaarOffer> getOrderedOffersForFruiton(final int fruitonId) {
        // TODO: check if fruiton with given id exists

        return bazaarOfferRepository.findByFruitonIdOrderByPriceAsc(fruitonId);
    }

    private static class BestOfferAggrResult {

        private int fruitonId;
        private int minPrice;

    }

}
