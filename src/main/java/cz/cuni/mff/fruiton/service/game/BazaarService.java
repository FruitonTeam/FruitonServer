package cz.cuni.mff.fruiton.service.game;

import cz.cuni.mff.fruiton.dao.domain.BazaarOffer;
import cz.cuni.mff.fruiton.dao.domain.User;

import java.util.List;

public interface BazaarService {

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

    void createOffer(User user, int fruitonId, int price);

    List<BazaarOfferListItemWithId> getOffersForUser(User user);

    void removeOffer(String offerId, User user);

    void buy(String offerId, User user);

    List<BazaarOffer> getOrderedOffersForFruiton(int fruitonId);

}
