package cz.cuni.mff.fruiton.component;

import cz.cuni.mff.fruiton.annotation.HandleProtobufMessage;
import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.CommonProtos;
import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.service.game.GameService;
import cz.cuni.mff.fruiton.service.game.matchmaking.MatchMakingService;
import cz.cuni.mff.fruiton.service.social.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// TODO: move these messages to the appropriate services

@Component
public final class Game {

    private final MatchMakingService matchMakingService;
    private final GameService gameService;
    private final UserService userService;

    @Autowired
    public Game(final MatchMakingService matchMakingService, final GameService gameService, final UserService userService) {
        this.matchMakingService = matchMakingService;
        this.gameService = gameService;
        this.userService = userService;
    }

    @HandleProtobufMessage(messageCase = CommonProtos.WrapperMessage.MessageCase.FINDGAME)
    public void handleFindGameMessage(final UserIdHolder from, final GameProtos.FindGame findGameMsg) {
        matchMakingService.findGame(from, findGameMsg);
    }

    @HandleProtobufMessage(messageCase = CommonProtos.WrapperMessage.MessageCase.CANCELFINDINGGAME)
    public void handleCancelFindingGameMessage(
            final UserIdHolder from,
            final GameProtos.CancelFindingGame cancelFindingGameMsg) {
        matchMakingService.removeFromMatchMaking(from);
    }

    @HandleProtobufMessage(messageCase = CommonProtos.WrapperMessage.MessageCase.PLAYERREADY)
    public void handlePlayerReadyMessage(final UserIdHolder from, final GameProtos.PlayerReady playerReadyMsg) {
        gameService.setPlayerReady(from);
    }

    @HandleProtobufMessage(messageCase = CommonProtos.WrapperMessage.MessageCase.ACTION)
    public void handleActionMessage(final UserIdHolder from, final GameProtos.Action actionMsg) {
        gameService.performAction(from, actionMsg);
    }

    @HandleProtobufMessage(messageCase = CommonProtos.WrapperMessage.MessageCase.SURRENDER)
    public void handleActionMessage(final UserIdHolder from, final GameProtos.Surrender surrenderMsg) {
        gameService.playerSurrendered(from);
    }

    @HandleProtobufMessage(messageCase = CommonProtos.WrapperMessage.MessageCase.SETFRACTION)
    public void handleSetFractionMessage(final UserIdHolder from, final GameProtos.SetFraction setFractionMsg) {
        userService.setFraction(from, setFractionMsg.getFraction());
    }

}
