package cz.cuni.mff.fruiton.dto.form;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public final class AddBazaarOfferForm {

    private int fruitonId;

    @Min(1)
    @NotNull
    private Integer price;

    public int getFruitonId() {
        return fruitonId;
    }

    public void setFruitonId(final int fruitonId) {
        this.fruitonId = fruitonId;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(final Integer price) {
        this.price = price;
    }

}
