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

    List<BazaarOfferListItem> getBestOffers();

    void createOffer(UserIdHolder idHolder, int fruitonId, int price);

    String createOffer(UserIdHolder offeredBy, UserIdHolder offeredTo, int fruitonId, int price);

    List<BazaarOfferListItemWithId> getOffersFromUser(UserIdHolder idHolder);

    void removeOffer(String offerId, UserIdHolder idHolder);

    void removeOfferByOfferedTo(String offerId, UserIdHolder offeredTo);

    void buy(String offerId, UserIdHolder idHolder, boolean boughtViaWeb);

    List<BazaarOffer> getOrderedOffersForFruiton(int fruitonId);

    int computeProfit(int price);

    List<BazaarOffer> getOffersOfferedTo(UserIdHolder offeredTo);

}
