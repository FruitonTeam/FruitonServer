package cz.cuni.mff.fruiton.service.game.bazaar;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dao.domain.BazaarOffer;

import java.util.List;

public interface BazaarService {

    float FEE = 0.05f;

    float PROFIT = 1 - FEE;

    class BazaarOfferListItem {

        private String fruitonName;
        private int price;
        private int fruitonId;

        public BazaarOfferListItem(final String fruitonName, final int price, final int fruitonId) {
            this.fruitonName = fruitonName;
            this.price = price;
            this.fruitonId = fruitonId;
        }

        public String getFruitonName() {
            return fruitonName;
        }

        public void setFruitonName(final String fruitonName) {
            this.fruitonName = fruitonName;
        }

        public int getPrice() {
            return price;
        }

        public void setPrice(final int price) {
            this.price = price;
        }

        public int getFruitonId() {
            return fruitonId;
        }

        public void setFruitonId(final int fruitonId) {
            this.fruitonId = fruitonId;
        }
    }

    class BazaarOfferListItemWithId {

        private String id;
        private BazaarOfferListItem item;

        public BazaarOfferListItemWithId(final String id, final BazaarOfferListItem item) {
            this.id = id;
            this.item = item;
        }

        public String getId() {
            return id;
        }

        public void setId(final String id) {
            this.id = id;
        }

        public BazaarOfferListItem getItem() {
            return item;
        }

        public void setItem(final BazaarOfferListItem item) {
            this.item = item;
        }
    }

    /**
     * Aggregates all bazaar offers and picks the best ones for each fruiton.
     * @return list of best offers, one for each fruiton
     */
    List<BazaarOfferListItem> getBestOffers();

    /**
     * Create generic bazaar offer visible to all users.
     * @param user user who creating an offer
     * @param fruitonId id of the fruiton for selling
     * @param price price for which other users can buy this offer
     */
    void createOffer(UserIdHolder user, int fruitonId, int price);

    /**
     * Creates specific bazaar offer visible only to {@code offeredTo} user.
     * @param offeredBy user who creating an offer
     * @param offeredTo user for whom the offer is created
     * @param fruitonId id of the fruiton for selling
     * @param price price for which other users can buy this offer
     * @return id of the newly created offer
     */
    String createOffer(UserIdHolder offeredBy, UserIdHolder offeredTo, int fruitonId, int price);

    /**
     * Returns all offer created by specified user.
     * @param user user who created returned offers
     * @return offers created by {@code user}
     */
    List<BazaarOfferListItemWithId> getOffersCreatedBy(UserIdHolder user);

    /**
     * Removes bazaar offer.
     * @param offerId id of the offer to remove
     * @param user user who is performing the removal
     */
    void removeOffer(String offerId, UserIdHolder user);

    /**
     * Removes bazaar offer by the user for whom it was created.
     * @param offerId id of the offer to remove
     * @param user user who is performing the removal
     */
    void removeOfferByOfferedTo(String offerId, UserIdHolder user);

    /**
     * Performs buy on the specified offer.
     * @param offerId id of the offer which is being bought
     * @param user user who is buying the offer
     * @param buyingViaWeb specifies if this offer is being bought from the web
     */
    void buy(String offerId, UserIdHolder user, boolean buyingViaWeb);

    /**
     * Returns all offers for specified fruiton.
     * @param fruitonId id of the fruiton
     * @return all offer for specified fruiton
     */
    List<BazaarOffer> getOrderedOffersForFruiton(int fruitonId);

    /**
     * Returns all offers that were offered to specified user.
     * @param offeredTo user for whom the returned offers were offered to
     * @return all offers that were offered to {@code offeredTo}
     */
    List<BazaarOffer> getOffersOfferedTo(UserIdHolder offeredTo);

}
