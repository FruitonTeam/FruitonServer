package cz.cuni.mff.fruiton.service.game.matchmaking.impl;

import cz.cuni.mff.fruiton.component.util.OnDisconnectedListener;
import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.CommonProtos;
import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.service.communication.CommunicationService;
import cz.cuni.mff.fruiton.service.communication.SessionService;
import cz.cuni.mff.fruiton.service.game.GameService;
import cz.cuni.mff.fruiton.service.game.matchmaking.ChallengeService;
import cz.cuni.mff.fruiton.service.social.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ChallengeServiceImpl implements ChallengeService, OnDisconnectedListener {

    private static final Logger logger = Logger.getLogger(ChallengeServiceImpl.class.getName());

    private final List<ChallengeData> challenges = new LinkedList<>();

    private final GameService gameService;

    private final SessionService sessionService;

    private final UserService userService;

    private final CommunicationService communicationService;

    @Autowired
    public ChallengeServiceImpl(
            final GameService gameService,
            final SessionService sessionService,
            final UserService userService,
            final CommunicationService communicationService
    ) {
        this.gameService = gameService;
        this.sessionService = sessionService;
        this.userService = userService;
        this.communicationService = communicationService;
    }

    @Override
    public void challenge(final UserIdHolder from, final GameProtos.Challenge challengeMsg) {
        UserIdHolder challenged = userService.findUserByLogin(challengeMsg.getChallengeFor());
        if (challenged == null) {
            logger.log(Level.WARNING, "Could not find challenged user " + challengeMsg.getChallengeFor());
            communicationService.send(from, getChallengeNotAcceptedMsg(from.getUsername()));
            return;
        }

        if (!sessionService.isOnline(challenged)) {
            logger.log(Level.WARNING, "Challenged user is not online");
            communicationService.send(from, getChallengeNotAcceptedMsg(from.getUsername()));
            return;
        }

        ChallengeData data = new ChallengeData(from, challengeMsg.getTeam(), challenged, challengeMsg.getMode());
        synchronized (challenges) {
            challenges.add(data);
        }
    }

    private CommonProtos.WrapperMessage getChallengeNotAcceptedMsg(final String challengeFrom) {
        return CommonProtos.WrapperMessage.newBuilder()
                .setChallengeResult(GameProtos.ChallengeResult.newBuilder()
                        .setChallengeAccepted(false)
                        .setChallengeFrom(challengeFrom))
                .build();
    }

    @Override
    public void handleChallengeResult(final UserIdHolder from, final GameProtos.ChallengeResult challengeResultMsg) {
        synchronized (challenges) {
            for (Iterator<ChallengeData> it = challenges.iterator(); it.hasNext(); ) {
                ChallengeData data = it.next();

                if (data.challenger.getUsername().equals(challengeResultMsg.getChallengeFrom())
                        && data.challenged.equals(from)) {

                    if (challengeResultMsg.getChallengeAccepted()) {
                        gameService.createGame(data.challenger, data.challengerTeam, data.challenged,
                                challengeResultMsg.getTeam(), data.gameMode);
                    } else {
                        communicationService.send(data.challenger,
                                getChallengeNotAcceptedMsg(challengeResultMsg.getChallengeFrom()));
                    }

                    it.remove();
                    break;
                }
            }
        }
    }

    @Override
    public void revokeChallenge(final UserIdHolder user) {
        synchronized (challenges) {
            for (Iterator<ChallengeData> it = challenges.iterator(); it.hasNext(); ) {
                ChallengeData data = it.next();
                if (data.challenger.equals(user)) {
                    sendRevokeMessage(data.challenged);
                    it.remove();
                    break;
                }
            }
        }
    }

    private void sendRevokeMessage(final UserIdHolder sendTo) {
        communicationService.send(sendTo, CommonProtos.WrapperMessage.newBuilder()
                .setRevokeChallenge(GameProtos.RevokeChallenge.newBuilder())
                .build());
    }

    @Override
    public void onDisconnected(final UserIdHolder user) {
        synchronized (challenges) {
            for (Iterator<ChallengeData> it = challenges.iterator(); it.hasNext(); ) {
                ChallengeData data = it.next();
                if (data.challenger.equals(user)) {
                    sendRevokeMessage(data.challenged);
                    it.remove();
                    break;
                } else if (data.challenged.equals(user)) {
                    communicationService.send(data.challenger, getChallengeNotAcceptedMsg(data.challenger.getUsername()));
                    it.remove();
                    break;
                }
            }
        }
    }

    private static class ChallengeData {

        private final UserIdHolder challenger;
        private final GameProtos.FruitonTeam challengerTeam;
        private final UserIdHolder challenged;
        private final GameProtos.GameMode gameMode;

        ChallengeData(
                final UserIdHolder challenger,
                final GameProtos.FruitonTeam challengerTeam,
                final UserIdHolder challenged,
                final GameProtos.GameMode mode
        ) {
            this.challenger = challenger;
            this.challengerTeam = challengerTeam;
            this.challenged = challenged;
            this.gameMode = mode;
        }
    }

}
