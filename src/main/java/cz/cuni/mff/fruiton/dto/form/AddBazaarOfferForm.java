package cz.cuni.mff.fruiton.dto.form;

import javax.validation.constraints.Min;

public final class AddBazaarOfferForm {

    private int fruitonId;

    @Min(1)
    private int price = 1;

    public int getFruitonId() {
        return fruitonId;
    }

    public void setFruitonId(final int fruitonId) {
        this.fruitonId = fruitonId;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(final int price) {
        this.price = price;
    }

}
