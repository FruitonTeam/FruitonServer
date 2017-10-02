package cz.cuni.mff.fruiton.util;

import cz.cuni.mff.fruiton.Application;
import cz.cuni.mff.fruiton.dto.GameProtos;
import fruiton.dataStructures.Point;
import fruiton.fruitDb.FruitonDatabase;
import fruiton.fruitDb.factories.FruitonFactory;
import fruiton.kernel.Fruiton;
import fruiton.kernel.actions.Action;
import fruiton.kernel.actions.AttackAction;
import fruiton.kernel.actions.MoveAction;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class KernelUtils {

    private enum ActionType {
        MOVE(0), ATTACK(1);

        private final int value;

        ActionType(final int value) {
            this.value = value;
        }

        public static ActionType parse(final int value) {
            for (ActionType actionType : ActionType.values()) {
                if (actionType.value == value) {
                    return actionType;
                }
            }

            throw new IllegalArgumentException("Unknown value " + value);
        }

    }

    private static FruitonDatabase fruitonDatabase;

    private KernelUtils() {
    }

    private static FruitonDatabase getFruitonDb() {
        if (fruitonDatabase == null) {
            synchronized (KernelUtils.class) {
                if (fruitonDatabase == null) {
                    try {
                        fruitonDatabase = new FruitonDatabase(String.join(System.lineSeparator(),
                                IOUtils.readLines(Application.class.getClassLoader()
                                        .getResourceAsStream("resources/FruitonDb.json"), StandardCharsets.UTF_8)));
                    } catch (IOException e) {
                        throw new IllegalStateException("Cannot get fruiton db", e);
                    }
                }
            }
        }

        return fruitonDatabase;
    }

    public static Fruiton getFruiton(final int id) {
        return FruitonFactory.makeFruiton(id, getFruitonDb());
    }

    public static boolean isActionWithTarget(final int actionId, final Action action, final GameProtos.Position target) {
        switch (ActionType.parse(actionId)) {
            case MOVE:
                return ((MoveAction) action).actionContext.target.equalsTo(positionToPoint(target));
            case ATTACK:
                return ((AttackAction) action).actionContext.target.equalsTo(positionToPoint(target));
            default:
                throw new IllegalStateException("Unknown action id");
        }
    }

    public static Point positionToPoint(final GameProtos.Position position) {
        return new Point(position.getX(), position.getY());
    }

}
