package cz.cuni.mff.fruiton.service.game.impl;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dao.domain.BazaarOffer;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dao.repository.BazaarOfferRepository;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.service.game.BazaarService;
import cz.cuni.mff.fruiton.service.game.FruitonService;
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
    private final UserRepository userRepository;

    private final UserService userService;

    private final FruitonService fruitonService;

    @Autowired
    public BazaarServiceImpl(
            final MongoTemplate mongoTemplate,
            final BazaarOfferRepository bazaarOfferRepository,
            final UserRepository userRepository,
            final UserService userService,
            final FruitonService fruitonService
    ) {
        this.mongoTemplate = mongoTemplate;
        this.bazaarOfferRepository = bazaarOfferRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.fruitonService = fruitonService;
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
    public void createOffer(final UserIdHolder idHolder, final int fruitonId, final int price) {
        if (idHolder == null) {
            throw new IllegalArgumentException("Null user cannot create offer");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Offer must have positive price");
        }
        User user = userRepository.findOne(idHolder.getId());
        if (!user.getUnlockedFruitons().contains(fruitonId)) {
            throw new IllegalArgumentException("User cannot sell fruiton he does not have");
        }

        BazaarOffer offer = new BazaarOffer(price, user, fruitonId);
        bazaarOfferRepository.save(offer);

        userService.removeFruitonFromUnlockedFruitons(idHolder, fruitonId);

        logger.log(Level.FINER, "Created offer {0}", offer);
    }

    @Override
    public List<BazaarOfferListItemWithId> getOffersForUser(final UserIdHolder idHolder) {
        User user = userRepository.findOne(idHolder.getId());

        return bazaarOfferRepository.findByOfferedBy(user).stream().map(offer -> {
            Fruiton f = KernelUtils.getFruiton(offer.getFruitonId());
            return new BazaarOfferListItemWithId(offer.getId(),
                    new BazaarOfferListItem(f.model, offer.getPrice(), offer.getFruitonId()));
        }).collect(Collectors.toList());
    }

    @Override
    public void removeOffer(final String offerId, final UserIdHolder idHolder) {
        BazaarOffer offer = bazaarOfferRepository.findOne(offerId);
        if (offer == null) {
            throw new IllegalArgumentException("No bazaar offer with id " + offerId);
        }

        if (!offer.getOfferedBy().getId().equals(idHolder.getId())) {
            throw new SecurityException("User " + idHolder + " cannot remove offer " + offer
                    + " because this offer was not offered by him");
        }

        userService.unlockFruiton(idHolder, offer.getFruitonId());
        bazaarOfferRepository.delete(offer);
    }

    @Override
    public void buy(final String offerId, final UserIdHolder idHolder) {
        if (idHolder == null) {
            throw new IllegalArgumentException("Null user cannot buy a fruiton");
        }

        BazaarOffer offer = bazaarOfferRepository.findOne(offerId);
        if (offer == null) {
            throw new IllegalArgumentException("No bazaar offer with id " + offerId);
        }

        if (offer.getOfferedBy().getId().equals(idHolder.getId())) {
            throw new IllegalStateException("User cannot buy his own offer");
        }

        User user = userRepository.findOne(idHolder.getId());
        if (user.getMoney() < offer.getPrice()) {
            throw new IllegalStateException("User has insufficient money to buy offer " + offer);
        }

        userService.unlockFruiton(idHolder, offer.getFruitonId());
        userService.adjustMoney(idHolder, -offer.getPrice());

        userService.adjustMoney(UserIdHolder.of(offer.getOfferedBy()), computeProfit(offer.getPrice()));

        bazaarOfferRepository.delete(offer);
    }

    @Override
    public List<BazaarOffer> getOrderedOffersForFruiton(final int fruitonId) {
        if (!fruitonService.exists(fruitonId)) {
            throw new IllegalArgumentException("No fruiton with id " + fruitonId + " exists");
        }

        return bazaarOfferRepository.findByFruitonIdOrderByPriceAsc(fruitonId);
    }

    @Override
    public int computeProfit(final int price) {
        return (int) (price * PROFIT);
    }

    private static class BestOfferAggrResult {

        private int fruitonId;
        private int minPrice;

    }

}
