package cz.cuni.mff.fruiton.dao.domain;

import cz.cuni.mff.fruiton.dto.GameProtos;

public final class QuestReward {

    private int money = 0;

    public int getMoney() {
        return money;
    }

    public void setMoney(final int money) {
        this.money = money;
    }

    public GameProtos.QuestReward toProtobuf() {
        return GameProtos.QuestReward.newBuilder().setMoney(money).build();
    }

}
