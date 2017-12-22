package cz.cuni.mff.fruiton.dao.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public final class BazaarOffer {

    @Id
    private String id;

    private int price;

    @DBRef
    private User offeredBy;

    private int fruitonId;

    public BazaarOffer() {
    }

    public BazaarOffer(final int price, final User offeredBy, final int fruitonId) {
        this.price = price;
        this.offeredBy = offeredBy;
        this.fruitonId = fruitonId;
    }

    public String getId() {
        return id;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(final int price) {
        this.price = price;
    }

    public User getOfferedBy() {
        return offeredBy;
    }

    public void setOfferedBy(final User offeredBy) {
        this.offeredBy = offeredBy;
    }

    public int getFruitonId() {
        return fruitonId;
    }

    public void setFruitonId(final int fruitonId) {
        this.fruitonId = fruitonId;
    }

    @Override
    public String toString() {
        return "BazaarOffer{"
                + "id='" + id + '\''
                + ", price=" + price
                + ", offeredBy=" + offeredBy.getLogin()
                + ", fruitonId=" + fruitonId
                + '}';
    }
}
