package cz.cuni.mff.fruiton.service.game;

import fruiton.kernel.Fruiton;

import java.util.List;

public interface FruitonService {

    class FruitonInfo {

        private int id;

        private String name;
        private int hp;
        private int damage;

        private FruitonInfo() {

        }

        public int getId() {
            return id;
        }

        public void setId(final int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public int getHp() {
            return hp;
        }

        public void setHp(final int hp) {
            this.hp = hp;
        }

        public int getDamage() {
            return damage;
        }

        public void setDamage(final int damage) {
            this.damage = damage;
        }

        public static FruitonInfo fromFruiton(final Fruiton fruiton) {
            if (fruiton == null) {
                throw new IllegalArgumentException("Cannot create fruiton info from null fruiton");
            }

            FruitonInfo info = new FruitonInfo();
            info.id = fruiton.id;
            info.name = fruiton.model;
            info.damage = fruiton.originalAttributes.damage;
            info.hp = fruiton.originalAttributes.hp;

            return info;
        }

    }

    List<FruitonInfo> getFruitonInfos(List<Integer> fruitonIds);

    FruitonInfo getFruitonInfo(int fruitonId);

}
