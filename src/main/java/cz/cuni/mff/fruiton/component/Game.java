package cz.cuni.mff.fruiton.component;

import cz.cuni.mff.fruiton.annotation.HandleProtobufMessage;
import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.CommonProtos;
import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.service.game.GameService;
import cz.cuni.mff.fruiton.service.game.matchmaking.MatchMakingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Game {

    private final MatchMakingService matchMakingService;
    private final GameService gameService;

    @Autowired
    public Game(final MatchMakingService matchMakingService, final GameService gameService) {
        this.matchMakingService = matchMakingService;
        this.gameService = gameService;
    }

    @HandleProtobufMessage(messageCase = CommonProtos.WrapperMessage.MessageCase.FINDGAME)
    public final void handleFindGameMessage(final UserIdHolder from, final GameProtos.FindGame findGameMsg) {
        matchMakingService.findGame(from, findGameMsg);
    }

    @HandleProtobufMessage(messageCase = CommonProtos.WrapperMessage.MessageCase.CANCELFINDINGGAME)
    public final void handleCancelFindingGameMessage(
            final UserIdHolder from,
            final GameProtos.CancelFindingGame cancelFindingGameMsg) {
        matchMakingService.removeFromMatchMaking(from);
    }

    @HandleProtobufMessage(messageCase = CommonProtos.WrapperMessage.MessageCase.PLAYERREADY)
    public final void handlePlayerReadyMessage(final UserIdHolder from, final GameProtos.PlayerReady playerReadyMsg) {
        gameService.setPlayerReady(from);
    }

    @HandleProtobufMessage(messageCase = CommonProtos.WrapperMessage.MessageCase.ACTION)
    public final void handleActionMessage(final UserIdHolder from, final GameProtos.Action actionMsg) {
        gameService.performAction(from, actionMsg);
    }

    @HandleProtobufMessage(messageCase = CommonProtos.WrapperMessage.MessageCase.SURRENDER)
    public final void handleActionMessage(final UserIdHolder from, final GameProtos.Surrender surrenderMsg) {
        gameService.playerSurrendered(from);
    }
}
