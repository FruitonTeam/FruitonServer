package cz.cuni.mff.fruiton.service.game.bazaar.impl;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dao.domain.BazaarOffer;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dao.repository.BazaarOfferRepository;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.dto.BazaarProtos;
import cz.cuni.mff.fruiton.dto.CommonProtos;
import cz.cuni.mff.fruiton.service.authentication.AuthenticationService;
import cz.cuni.mff.fruiton.service.communication.CommunicationService;
import cz.cuni.mff.fruiton.service.communication.SessionService;
import cz.cuni.mff.fruiton.service.game.bazaar.BazaarService;
import cz.cuni.mff.fruiton.service.game.FruitonService;
import cz.cuni.mff.fruiton.service.social.UserService;
import cz.cuni.mff.fruiton.util.KernelUtils;
import fruiton.kernel.Fruiton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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

    private final CommunicationService communicationService;

    private final SessionService sessionService;

    private final AuthenticationService authService;

    private final Object lock = new Object();

    @Autowired
    public BazaarServiceImpl(
            final MongoTemplate mongoTemplate,
            final BazaarOfferRepository bazaarOfferRepository,
            final UserRepository userRepository,
            final UserService userService,
            final FruitonService fruitonService,
            final CommunicationService communicationService,
            final SessionService sessionService,
            final AuthenticationService authService
    ) {
        this.mongoTemplate = mongoTemplate;
        this.bazaarOfferRepository = bazaarOfferRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.fruitonService = fruitonService;
        this.communicationService = communicationService;
        this.sessionService = sessionService;
        this.authService = authService;
    }

    @Override
    public List<BazaarOfferListItem> getBestOffers() {
        MatchOperation match = Aggregation.match(
                new Criteria().orOperator(
                        Criteria.where("offeredTo").is(null), // we cannot use .in() because null will cause error
                        Criteria.where("offeredTo").is(authService.getLoggedInUser().getId())));

        GroupOperation groupBy = Aggregation.group("fruitonId").min("price").as("minPrice");
        ProjectionOperation projection = Aggregation.project("minPrice").and("fruitonId").previousOperation();

        TypedAggregation<BazaarOffer> aggregation = Aggregation.newAggregation(BazaarOffer.class, match, groupBy, projection);

        AggregationResults<BestOfferAggrResult> result = mongoTemplate.aggregate(aggregation, BestOfferAggrResult.class);

        List<BazaarOfferListItem> listItems = new ArrayList<>(result.getMappedResults().size());

        for (BestOfferAggrResult res : result) {
            Fruiton f = KernelUtils.getFruiton(res.fruitonId);
            BazaarOfferListItem listItem = new BazaarOfferListItem(f.name, res.minPrice, res.fruitonId);
            listItems.add(listItem);
        }

        return listItems;
    }

    @Override
    public void createOffer(final UserIdHolder idHolder, final int fruitonId, final int price) {
        createOffer(idHolder, null, fruitonId, price);
    }

    @Override
    public String createOffer(final UserIdHolder offeredBy, final UserIdHolder offeredTo, final int fruitonId, final int price) {
        if (offeredBy == null) {
            throw new IllegalArgumentException("Null user cannot create offer");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Offer must have positive price");
        }

        synchronized (lock) {
            User user = userRepository.findOne(offeredBy.getId());
            if (!user.getUnlockedFruitons().contains(fruitonId)) {
                throw new IllegalArgumentException("User cannot sell fruiton he does not have");
            }

            BazaarOffer offer;
            if (offeredTo == null) {
                offer = new BazaarOffer(price, user, fruitonId);
            } else {
                offer = new BazaarOffer(price, user, fruitonId, userRepository.findOne(offeredTo.getId()));
            }
            bazaarOfferRepository.save(offer);

            userService.removeFruitonFromUnlockedFruitons(offeredBy, fruitonId);

            logger.log(Level.FINER, "Created offer {0}", offer);

            return offer.getId();
        }
    }

    @Override
    public List<BazaarOfferListItemWithId> getOffersFromUser(final UserIdHolder idHolder) {
        User user = userRepository.findOne(idHolder.getId());

        return bazaarOfferRepository.findByOfferedBy(user).stream().map(offer -> {
            Fruiton f = KernelUtils.getFruiton(offer.getFruitonId());
            return new BazaarOfferListItemWithId(offer.getId(),
                    new BazaarOfferListItem(f.name, offer.getPrice(), offer.getFruitonId()));
        }).collect(Collectors.toList());
    }

    @Override
    public void removeOffer(final String offerId, final UserIdHolder idHolder) {
        synchronized (lock) {
            BazaarOffer offer = bazaarOfferRepository.findOne(offerId);
            if (offer == null) {
                throw new IllegalArgumentException("No bazaar offer with id " + offerId);
            }

            if (!offer.getOfferedBy().getId().equals(idHolder.getId())) {
                throw new SecurityException("User " + idHolder + " cannot remove offer " + offer
                        + " because this offer was not offered by him");
            }

            if (offer.getOfferedTo() != null) {
                UserIdHolder offeredTo = UserIdHolder.of(offer.getOfferedTo());
                if (sessionService.isOnline(offeredTo)) {
                    sendBazaarOfferResolvedOnTheWeb(offeredTo, offerId);
                }
            }

            userService.unlockFruiton(idHolder, offer.getFruitonId());
            bazaarOfferRepository.delete(offer);
        }
    }

    @Override
    public void removeOfferByOfferedTo(final String offerId, final UserIdHolder offeredTo) {
        synchronized (lock) {
            BazaarOffer offer = bazaarOfferRepository.findOne(offerId);
            if (offer == null) {
                throw new IllegalArgumentException("No bazaar offer with id " + offerId);
            }

            if (!offeredTo.represents(offer.getOfferedTo())) {
                throw new SecurityException("User " + offeredTo + " cannot remove offer " + offer
                        + " because this offer was not offered to him");
            }

            userService.unlockFruiton(UserIdHolder.of(offer.getOfferedBy()), offer.getFruitonId());
            bazaarOfferRepository.delete(offer);
        }
    }

    @Override
    public void buy(final String offerId, final UserIdHolder idHolder, final boolean boughtViaWeb) {
        if (idHolder == null) {
            throw new IllegalArgumentException("Null user cannot buy a fruiton");
        }

        synchronized (lock) {
            BazaarOffer offer = bazaarOfferRepository.findOne(offerId);
            if (offer == null) {
                throw new IllegalArgumentException("No bazaar offer with id " + offerId);
            }

            if (offer.getOfferedBy().getId().equals(idHolder.getId())) {
                throw new IllegalStateException("User cannot buy his own offer");
            }

            if (offer.getOfferedTo() != null && !idHolder.represents(offer.getOfferedTo())) {
                throw new IllegalStateException("Offer " + offerId + " was not created for " + idHolder);
            }

            User user = userRepository.findOne(idHolder.getId());

            if (user.getMoney() < offer.getPrice()) {
                throw new IllegalStateException("User has insufficient money to buy offer " + offer);
            }

            userService.unlockFruiton(idHolder, offer.getFruitonId());
            userService.adjustMoney(idHolder, -offer.getPrice());


            int profit;
            if (offer.getOfferedTo() == null) {
                profit = computeProfit(offer.getPrice());
            } else {
                profit = offer.getPrice();
            }

            UserIdHolder offeredBy = UserIdHolder.of(offer.getOfferedBy());
            userService.adjustMoney(offeredBy, profit);

            if (sessionService.isOnline(offeredBy)) {
                sendBazaarOfferResult(offeredBy, profit);
            }

            if (boughtViaWeb && sessionService.isOnline(idHolder)) {
                sendBazaarOfferResolvedOnTheWeb(idHolder, offerId);
            }

            bazaarOfferRepository.delete(offer);
        }
    }

    private void sendBazaarOfferResolvedOnTheWeb(final UserIdHolder to, final String offerId) {
        communicationService.send(to, CommonProtos.WrapperMessage.newBuilder()
                .setBazaarOfferResolvedOnTheWeb(BazaarProtos.BazaarOfferResolvedOnTheWeb.newBuilder()
                        .setOfferId(offerId))
                .build());
    }

    @Override
    public List<BazaarOffer> getOrderedOffersForFruiton(final int fruitonId) {
        if (!fruitonService.exists(fruitonId)) {
            throw new IllegalArgumentException("No fruiton with id " + fruitonId + " exists");
        }

        Query query = new Query()
                .addCriteria(Criteria.where("fruitonId").is(fruitonId))
                .addCriteria(
                        new Criteria().orOperator(
                                Criteria.where("offeredTo").is(null), // we cannot use .in() because null will cause error
                                Criteria.where("offeredTo").is(authService.getLoggedInUser().getId())))
                .with(new Sort(Sort.Direction.ASC, "price"));

        return mongoTemplate.find(query, BazaarOffer.class);
    }

    @Override
    public int computeProfit(final int price) {
        return (int) (price * PROFIT);
    }

    @Override
    public List<BazaarOffer> getOffersOfferedTo(final UserIdHolder offeredTo) {
        return bazaarOfferRepository.findByOfferedTo(userRepository.findOne(offeredTo.getId()));
    }

    private void sendBazaarOfferResult(final UserIdHolder to, final int money) {
        communicationService.send(to, CommonProtos.WrapperMessage.newBuilder()
                .setBazaarOfferResult(BazaarProtos.BazaarOfferResult.newBuilder()
                        .setMoneyChange(money)
                        .build())
                .build());
    }

    private static class BestOfferAggrResult {

        private int fruitonId;
        private int minPrice;

    }

}
