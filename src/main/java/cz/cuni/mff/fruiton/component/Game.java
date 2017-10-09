package cz.cuni.mff.fruiton.component;

import cz.cuni.mff.fruiton.annotation.HandleProtobufMessage;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dto.CommonProtos;
import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.service.game.GameService;
import cz.cuni.mff.fruiton.service.game.MatchMakingService;
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
    public final void handleFindGameMessage(final User from, final GameProtos.FindGame findGameMsg) {
        matchMakingService.findGame(from, findGameMsg);
    }

    @HandleProtobufMessage(messageCase = CommonProtos.WrapperMessage.MessageCase.CANCELFINDINGGAME)
    public final void handleCancelFindingGameMessage(final User from, final GameProtos.CancelFindingGame cancelFindingGameMsg) {
        matchMakingService.removeFromMatchMaking(from);
    }

    @HandleProtobufMessage(messageCase = CommonProtos.WrapperMessage.MessageCase.PLAYERREADY)
    public final void handlePlayerReadyMessage(final User from, final GameProtos.PlayerReady playerReadyMsg) {
        gameService.setPlayerReady(from);
    }

    @HandleProtobufMessage(messageCase = CommonProtos.WrapperMessage.MessageCase.ACTION)
    public final void handleActionMessage(final User from, final GameProtos.Action actionMsg) {
        gameService.performAction(from, actionMsg);
    }

}