package cz.cuni.mff.fruiton.service.game;

import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.util.HaxeUtils;
import fruiton.kernel.AttackGenerator;
import fruiton.kernel.Fruiton;
import fruiton.kernel.MoveGenerator;
import fruiton.kernel.actions.AttackAction;
import fruiton.kernel.actions.HealAction;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

public interface FruitonService {

    enum FruitonType {
        MINOR(3, "Minor"), MAJOR(2, "Major"), KING(1, "King");

        private final int typeId;
        private final String name;

        FruitonType(final int typeId, final String name) {
            this.typeId = typeId;
            this.name = name;
        }

        public static FruitonType fromTypeId(final int typeId) {
            for (FruitonType type : FruitonType.values()) {
                if (type.typeId == typeId) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown fruiton type " + typeId);
        }

        public static FruitonType fromProtobuf(final GameProtos.FruitonType type) {
            switch (type) {
                case KING:
                    return FruitonType.KING;
                case MAJOR:
                    return FruitonType.MAJOR;
                case MINOR:
                    return FruitonType.MINOR;
                default:
                    throw new IllegalArgumentException("Unknown type " + type);
            }
        }

        @Override
        public String toString() {
            return name;
        }
    }

    class FruitonInfo {

        private static final String HEAL_IMMUNITY_DESC = "Can't be healed.";
        private static final String ATTACK_IMMUNITY_DESC = "Can't be attacked";

        private int id;

        private FruitonType type;

        private String name;
        private int hp;
        private int damage;
        private String model;
        private String attack = "";
        private String movement = "";
        private String abilities = "";
        private String effects = "";
        private String immunities = "";

        private FruitonInfo() {

        }

        public int getId() {
            return id;
        }

        public FruitonType getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public int getHp() {
            return hp;
        }

        public int getDamage() {
            return damage;
        }

        public String getModel() {
            return model;
        }

        public String getAttack() {
            return attack;
        }

        public String getMovement() {
            return movement;
        }

        public String getAbilities() {
            return abilities;
        }

        public String getEffects() {
            return effects;
        }

        public String getImmunities() {
            return immunities;
        }

        public static FruitonInfo fromFruiton(final Fruiton fruiton) {
            if (fruiton == null) {
                throw new IllegalArgumentException("Cannot create fruiton info from null fruiton");
            }

            FruitonInfo info = new FruitonInfo();
            info.id = fruiton.dbId;
            info.type = FruitonType.fromTypeId(fruiton.type);
            info.name = fruiton.name;
            info.model = fruiton.model;
            info.damage = fruiton.originalAttributes.damage;
            info.hp = fruiton.originalAttributes.hp;

            info.attack = HaxeUtils.toStream(fruiton.attackGenerators).map(AttackGenerator::toString)
                    .collect(Collectors.joining("\n"));
            info.movement = HaxeUtils.toStream(fruiton.moveGenerators).map(MoveGenerator::toString)
                    .collect(Collectors.joining("\n"));
            info.abilities = HaxeUtils.toStream(fruiton.abilities)
                    .map(a -> MessageFormat.format(a.text, fruiton.originalAttributes.heal))
                    .collect(Collectors.joining("\n"));
            info.effects = HaxeUtils.toStream(fruiton.effects).map(e -> e.text).collect(Collectors.joining("\n"));

            for (Object immunity : HaxeUtils.toList(fruiton.currentAttributes.immunities)) {
                int immunityInt = (Integer) immunity;
                if (immunityInt == HealAction.ID) {
                    if (!info.immunities.isEmpty()) {
                        info.immunities += "\n";
                    }
                    info.immunities += HEAL_IMMUNITY_DESC;
                } else if (immunityInt == AttackAction.ID) {
                    if (!info.immunities.isEmpty()) {
                        info.immunities += "\n";
                    }
                    info.immunities += ATTACK_IMMUNITY_DESC;
                }
            }

            return info;
        }
    }

    List<FruitonInfo> getFruitonInfos(List<Integer> fruitonIds);

    FruitonInfo getFruitonInfo(int fruitonId);

    List<Integer> getRandomFruitons();

    List<Integer> getRandomFruitons(int count, List<Integer> excludedFruitons);

    List<Integer> filter(List<Integer> fruitonIds, FruitonType type);

    boolean exists(int fruitonId);

}
