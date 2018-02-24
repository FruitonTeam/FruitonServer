package cz.cuni.mff.fruiton.util;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dao.domain.FruitonTeam;
import cz.cuni.mff.fruiton.dto.CommonProtos.ErrorMessage.ErrorId;
import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.exception.FruitonServerException;
import cz.cuni.mff.fruiton.service.social.UserService;

import java.util.ArrayList;
import java.util.List;

public class FruitonTeamUtils {

    public static class InvalidTeamException extends FruitonServerException {

        private final GameProtos.FruitonTeam team;

        public InvalidTeamException(final GameProtos.FruitonTeam team) {
            super("Invalid team: " + team, ErrorId.INVALID_TEAM);
            this.team = team;
        }

        public final GameProtos.FruitonTeam getTeam() {
            return team;
        }

    }

    public static class NotUnlockedFruitonException extends FruitonServerException {

        private final List<Integer> notUnlockedFruitons;

        public NotUnlockedFruitonException(final List<Integer> notUnlockedFruitons) {
            super("Not unlocked fruitons: " + notUnlockedFruitons, ErrorId.USE_OF_NOT_UNLOCKED_FRUITON);
            this.notUnlockedFruitons = notUnlockedFruitons;
        }

        public final List<Integer> getNotUnlockedFruitons() {
            return notUnlockedFruitons;
        }
    }

    private FruitonTeamUtils() {
    }

    public static void checkTeamValidity(
            final UserIdHolder user,
            final GameProtos.FruitonTeam fruitonTeam,
            final UserService userService
    ) {
        if (!KernelUtils.isTeamValid(fruitonTeam)) {
            throw new InvalidTeamException(fruitonTeam);
        } else if (!userService.teamContainsAvailableFruitons(user, FruitonTeam.fromProtobuf(fruitonTeam))) {
            List<Integer> fruitonsList = new ArrayList<>(fruitonTeam.getFruitonIDsList());
            fruitonsList.removeAll(userService.getAvailableFruitons(user));
            throw new NotUnlockedFruitonException(fruitonsList);
        }
    }

    public static boolean isTeamValid(
            final UserIdHolder user,
            final GameProtos.FruitonTeam fruitonTeam,
            final UserService userService
    ) {
        return KernelUtils.isTeamValid(fruitonTeam)
                && userService.teamContainsAvailableFruitons(
                        user, cz.cuni.mff.fruiton.dao.domain.FruitonTeam.fromProtobuf(fruitonTeam));
    }

}
