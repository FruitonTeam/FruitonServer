package cz.cuni.mff.fruiton.util;

import cz.cuni.mff.fruiton.Application;
import cz.cuni.mff.fruiton.dto.GameProtos;
import fruiton.dataStructures.Point;
import fruiton.fruitDb.FruitonDatabase;
import fruiton.fruitDb.factories.FruitonFactory;
import fruiton.kernel.Fruiton;
import fruiton.kernel.actions.Action;
import fruiton.kernel.actions.AttackAction;
import fruiton.kernel.actions.AttackActionContext;
import fruiton.kernel.actions.EndTurnAction;
import fruiton.kernel.actions.EndTurnActionContext;
import fruiton.kernel.actions.MoveAction;
import fruiton.kernel.actions.MoveActionContext;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class KernelUtils {

    public static final int MAP_WIDTH = 9;
    public static final int MAP_HEIGHT = 10;

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

    public static Action getActionFromProtobuf(final GameProtos.Action protobufAction) {
        if (MoveAction.ID == protobufAction.getId()) {
            return new MoveAction(new MoveActionContext(
                    KernelUtils.positionToPoint(protobufAction.getFrom()),
                    KernelUtils.positionToPoint(protobufAction.getTo())
            ));
        } else if (AttackAction.ID == protobufAction.getId()) {
            return new AttackAction(new AttackActionContext(
                    0,
                    KernelUtils.positionToPoint(protobufAction.getFrom()),
                    KernelUtils.positionToPoint(protobufAction.getTo())
            ));
        } else if (EndTurnAction.ID == protobufAction.getId()) {
            return new EndTurnAction(new EndTurnActionContext());
        } else {
            throw new IllegalStateException("Unknown action " + protobufAction);
        }
    }


    public static Point positionToPoint(final GameProtos.Position position) {
        return new Point(position.getX(), position.getY());
    }

    public static GameProtos.Position positionOf(final int x, final int y) {
        return GameProtos.Position.newBuilder().setX(x).setY(y).build();
    }

}
