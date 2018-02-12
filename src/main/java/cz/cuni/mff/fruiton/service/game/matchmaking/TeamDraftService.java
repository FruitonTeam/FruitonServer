package cz.cuni.mff.fruiton.service.game.matchmaking;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.GameProtos.GameMode;

public interface TeamDraftService {

    void startDraft(UserIdHolder player1, UserIdHolder player2, GameMode gameMode);

}
