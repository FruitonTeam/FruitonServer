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
import cz.cuni.mff.fruiton.service.util.UserStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public final class ChallengeServiceImpl implements ChallengeService, OnDisconnectedListener,
        UserStateService.OnUserStateChangedListener {

    private static final Logger logger = Logger.getLogger(ChallengeServiceImpl.class.getName());

    private final List<ChallengeData> challenges = new LinkedList<>();

    private final GameService gameService;

    private final SessionService sessionService;

    private final UserService userService;

    private final CommunicationService communicationService;

    private final UserStateService userStateService;

    @Autowired
    public ChallengeServiceImpl(
            final GameService gameService,
            final SessionService sessionService,
            final UserService userService,
            final CommunicationService communicationService,
            final UserStateService userStateService
    ) {
        this.gameService = gameService;
        this.sessionService = sessionService;
        this.userService = userService;
        this.communicationService = communicationService;
        this.userStateService = userStateService;
    }

    @PostConstruct
    private void init() {
        // done differently than with `OnDisconnectedListener` to avoid cyclic dependency
        userStateService.addListener(this);
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

        if (userStateService.getState(challenged) != UserStateService.UserState.MAIN_MENU) {
            logger.log(Level.WARNING, "Challenged user is not in main menu");
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
        ChallengeData dataForGameCreation = null;
        synchronized (challenges) {
            for (Iterator<ChallengeData> it = challenges.iterator(); it.hasNext();) {
                ChallengeData data = it.next();

                if (data.challenger.getUsername().equals(challengeResultMsg.getChallengeFrom())
                        && data.challenged.equals(from)) {

                    if (challengeResultMsg.getChallengeAccepted()) {
                        dataForGameCreation = data; // delay game creation, explained below
                    } else {
                        communicationService.send(data.challenger,
                                getChallengeNotAcceptedMsg(challengeResultMsg.getChallengeFrom()));
                    }

                    it.remove();
                    break;
                }
            }

            // game creation is delayed outside of loop so `onUserStateChanged` method can perform correctly
            // (it.remove() - The behavior of an iterator is unspecified if the underlying collection is modified while
            // the iteration is in progress in any way other than by calling this method.)
            if (dataForGameCreation != null) {
                gameService.createGame(dataForGameCreation.challenger, dataForGameCreation.challengerTeam,
                        dataForGameCreation.challenged, challengeResultMsg.getTeam(), dataForGameCreation.gameMode);
            }
        }
    }

    @Override
    public void revokeChallenge(final UserIdHolder user) {
        synchronized (challenges) {
            for (Iterator<ChallengeData> it = challenges.iterator(); it.hasNext();) {
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
        removeFromChallenges(user);
    }

    private void removeFromChallenges(final UserIdHolder user) {
        synchronized (challenges) {
            for (Iterator<ChallengeData> it = challenges.iterator(); it.hasNext();) {
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

    @Override
    public void onUserStateChanged(final UserIdHolder user, final UserStateService.UserState newState) {
        if (newState == UserStateService.UserState.IN_MATCHMAKING || newState == UserStateService.UserState.IN_BATTLE) {
            removeFromChallenges(user);
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
