package cz.cuni.mff.fruiton.util;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.service.social.UserService;

public class FruitonTeamUtils {

    private FruitonTeamUtils() {
    }

    public static boolean isTeamValid(
            final UserIdHolder user,
            final GameProtos.FruitonTeam fruitonTeam,
            final UserService userService
    ) {
        return KernelUtils.isTeamValid(fruitonTeam)
                && userService.teamContainsUnlockedFruitons(
                        user, cz.cuni.mff.fruiton.dao.domain.FruitonTeam.fromProtobuf(fruitonTeam));
    }

}
