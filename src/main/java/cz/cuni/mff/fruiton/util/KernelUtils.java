package cz.cuni.mff.fruiton.util;

import cz.cuni.mff.fruiton.Application;
import cz.cuni.mff.fruiton.dto.GameProtos;
import fruiton.dataStructures.Point;
import fruiton.fruitDb.FruitonDatabase;
import fruiton.fruitDb.factories.FruitonFactory;
import fruiton.fruitDb.factories.MapFactory;
import fruiton.kernel.Fruiton;
import fruiton.kernel.GameSettings;
import fruiton.kernel.Kernel;
import fruiton.kernel.actions.Action;
import fruiton.kernel.actions.AttackAction;
import fruiton.kernel.actions.AttackActionContext;
import fruiton.kernel.actions.EndTurnAction;
import fruiton.kernel.actions.HealAction;
import fruiton.kernel.actions.HealActionContext;
import fruiton.kernel.actions.MoveAction;
import fruiton.kernel.actions.MoveActionContext;
import fruiton.kernel.fruitonTeam.FruitonTeamValidator;
import fruiton.kernel.fruitonTeam.ValidationResult;
import fruiton.kernel.gameModes.GameMode;
import fruiton.kernel.gameModes.LastManStandingGameMode;
import fruiton.kernel.gameModes.StandardGameMode;
import haxe.root.Array;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public final class KernelUtils {

    private static final int TUTORIAL_FRUITONS_ID_START = 1001;

    private static final Random RANDOM = new Random();

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

    public static List<Integer> getAllFruitonIds() {
        return Arrays.stream(getFruitonDb().fruitonDb._keys)
                .filter(key -> key != 0) // haxe awkwardness -> _keys contain 0 which is not really there
                .filter(key -> key < TUTORIAL_FRUITONS_ID_START) // filter out fruitons which are used for tutorial (1001, ...)
                .boxed().collect(Collectors.toList());
    }

    public static Action getActionFromProtobuf(final GameProtos.Action protobufAction, final Kernel kernel) {
        if (MoveAction.ID == protobufAction.getId()) {
            return new MoveAction(new MoveActionContext(
                    KernelUtils.positionToPoint(protobufAction.getFrom()),
                    KernelUtils.positionToPoint(protobufAction.getTo())
            ));
        } else if (AttackAction.ID == protobufAction.getId()) {
            Point from = KernelUtils.positionToPoint(protobufAction.getFrom());
            int dmg = kernel.currentState.field.get(from).fruiton.currentAttributes.damage;

            return new AttackAction(new AttackActionContext(dmg, from,
                    KernelUtils.positionToPoint(protobufAction.getTo())
            ));
        } else if (HealAction.ID == protobufAction.getId()) {
            Point from = KernelUtils.positionToPoint(protobufAction.getFrom());
            int heal = kernel.currentState.field.get(from).fruiton.currentAttributes.heal;
            return new HealAction(new HealActionContext(heal, from,
                    KernelUtils.positionToPoint(protobufAction.getTo())
            ));
        } else if (EndTurnAction.ID == protobufAction.getId()) {
            return EndTurnAction.createNew();
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

    public static boolean isTeamValid(final GameProtos.FruitonTeam team) {
        Array<Object> fruitons = new Array<>();
        team.getFruitonIDsList().forEach(fruitons::push);

        ValidationResult validationResult = FruitonTeamValidator.validateFruitonTeam(fruitons, getFruitonDb());
        return validationResult.complete && validationResult.valid;
    }

    public static int getRandomMapId() {
        Array<Object> mapIds = getFruitonDb().getMapsIds();
        return (Integer) mapIds.__get(RANDOM.nextInt(mapIds.length));
    }

    public static GameSettings makeGameSettings(final int mapId, final GameProtos.GameMode gameModeType) {
        GameMode gameMode;
        switch (gameModeType) {
            case STANDARD:
                gameMode = new StandardGameMode();
                break;
            case LAST_MAN_STANDING:
                gameMode = new LastManStandingGameMode();
                break;
            default:
                throw new IllegalArgumentException("Game mode not supported");
        }

        return new GameSettings(MapFactory.makeMap(mapId, getFruitonDb()), gameMode);
    }

}
