package cz.cuni.mff.fruiton.component;

import cz.cuni.mff.fruiton.annotation.ProtobufMessage;
import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.CommonProtos.WrapperMessage;
import cz.cuni.mff.fruiton.dto.CommonProtos.WrapperMessage.MessageCase;
import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.dto.GameProtos.Challenge;
import cz.cuni.mff.fruiton.dto.GameProtos.ChallengeResult;
import cz.cuni.mff.fruiton.dto.GameProtos.FruitonTeam;
import cz.cuni.mff.fruiton.dto.GameProtos.GameMode;
import cz.cuni.mff.fruiton.dto.GameProtos.PickMode;
import cz.cuni.mff.fruiton.dto.GameProtos.Status;
import cz.cuni.mff.fruiton.service.communication.CommunicationService;
import cz.cuni.mff.fruiton.service.communication.SessionService;
import cz.cuni.mff.fruiton.service.game.AchievementService;
import cz.cuni.mff.fruiton.service.game.GameService;
import cz.cuni.mff.fruiton.service.game.matchmaking.TeamDraftService;
import cz.cuni.mff.fruiton.service.social.UserService;
import cz.cuni.mff.fruiton.service.util.UserStateService;
import cz.cuni.mff.fruiton.service.util.UserStateService.OnUserStateChangedListener;
import cz.cuni.mff.fruiton.util.FruitonTeamUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public final class Challenges implements OnUserStateChangedListener {

    private static final String CHALLENGE_ACHV_NAME = "Challenge";

    private static final Logger logger = Logger.getLogger(Challenges.class.getName());

    private final List<ChallengeData> challenges = new LinkedList<>();

    private final GameService gameService;

    private final SessionService sessionService;

    private final UserService userService;

    private final CommunicationService communicationService;

    private final UserStateService userStateService;

    private final AchievementService achievementService;

    private final TeamDraftService draftService;

    @Autowired
    public Challenges(
            final GameService gameService,
            final SessionService sessionService,
            final UserService userService,
            final CommunicationService communicationService,
            final UserStateService userStateService,
            final AchievementService achievementService,
            final TeamDraftService draftService
    ) {
        this.gameService = gameService;
        this.sessionService = sessionService;
        this.userService = userService;
        this.communicationService = communicationService;
        this.userStateService = userStateService;
        this.achievementService = achievementService;
        this.draftService = draftService;
    }

    @PostConstruct
    private void init() {
        userStateService.addListener(this);
    }

    @ProtobufMessage(messageCase = MessageCase.CHALLENGE)
    public void challenge(final UserIdHolder from, final Challenge challengeMsg) {
        UserIdHolder challenged = userService.tryFindUserByLogin(challengeMsg.getChallengeFor());
        if (challenged == null) {
            logger.log(Level.WARNING, "Could not find challenged user {0}", challengeMsg.getChallengeFor());
            communicationService.send(from, getChallengeNotAcceptedMsg(from.getUsername()));
            return;
        }

        if (from.equals(challenged)) { // user cannot challenge himself
            logger.log(Level.WARNING, "User tried to challenge himself: {0}", from);
            communicationService.send(from, getChallengeNotAcceptedMsg(from.getUsername()));
            return;
        }

        if (!sessionService.isOnline(challenged)) {
            logger.log(Level.WARNING, "Challenged user is not online");
            communicationService.send(from, getChallengeNotAcceptedMsg(from.getUsername()));
            return;
        }

        if (userStateService.getState(challenged) != Status.MAIN_MENU) {
            logger.log(Level.WARNING, "Challenged user is not in main menu");
            communicationService.send(from, getChallengeNotAcceptedMsg(from.getUsername()));
            return;
        }

        if (challengeMsg.getPickMode() == PickMode.STANDARD_PICK) {
            FruitonTeamUtils.checkTeamValidity(from, challengeMsg.getTeam(), userService);
        }

        ChallengeData data = new ChallengeData(from, challengeMsg.getTeam(), challenged, challengeMsg.getGameMode(),
                challengeMsg.getPickMode());

        synchronized (challenges) {
            challenges.add(data);
        }

        logger.log(Level.FINER, "Challenge added from {0} for {1}", new Object[] {data.challenger, data.challenged});

        // send challenge message to other user
        if (from.getUsername().equals(challengeMsg.getChallengeFrom())) {
            communicationService.send(challenged, WrapperMessage.newBuilder().setChallenge(challengeMsg).build());
        } else { // repair challenge's from field
            Challenge challenge = Challenge.newBuilder(challengeMsg).setChallengeFrom(from.getUsername()).build();
            communicationService.send(challenged, WrapperMessage.newBuilder().setChallenge(challenge).build());
        }
    }

    private WrapperMessage getChallengeNotAcceptedMsg(final String challengeFrom) {
        return WrapperMessage.newBuilder()
                .setChallengeResult(ChallengeResult.newBuilder()
                        .setChallengeAccepted(false)
                        .setChallengeFrom(challengeFrom))
                .build();
    }

    @ProtobufMessage(messageCase = MessageCase.CHALLENGERESULT)
    private void handleChallengeResult(final UserIdHolder from, final ChallengeResult challengeResultMsg) {
        logger.log(Level.FINER, "Received challenge result from {0} : {1}", new Object[] {from, challengeResultMsg});

        ChallengeData dataForGameCreation = null;
        synchronized (challenges) {
            for (Iterator<ChallengeData> it = challenges.iterator(); it.hasNext();) {
                ChallengeData data = it.next();

                if (data.challenger.getUsername().equals(challengeResultMsg.getChallengeFrom())
                        && data.challenged.equals(from)) {

                    if (challengeResultMsg.getChallengeAccepted()) {
                        if (data.pickMode == PickMode.STANDARD_PICK) {
                            FruitonTeamUtils.checkTeamValidity(from, challengeResultMsg.getTeam(), userService);
                        }

                        communicationService.send(data.challenger, WrapperMessage.newBuilder()
                                .setChallengeResult(challengeResultMsg)
                                .build());
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
                logger.log(Level.FINE, "Creating challenge game between {0} and {1}",
                        new Object[] {dataForGameCreation.challenger, dataForGameCreation.challenged});

                if (dataForGameCreation.pickMode == PickMode.STANDARD_PICK) {
                    gameService.createGame(dataForGameCreation.challenger, dataForGameCreation.challengerTeam,
                            dataForGameCreation.challenged, challengeResultMsg.getTeam(), dataForGameCreation.gameMode);
                } else {
                    draftService.startDraft(dataForGameCreation.challenger, dataForGameCreation.challenged,
                            dataForGameCreation.gameMode);
                }
                unlockChallengeAchievement(dataForGameCreation.challenger);
                unlockChallengeAchievement(dataForGameCreation.challenged);
            }
        }
    }

    private void unlockChallengeAchievement(final UserIdHolder user) {
        achievementService.unlockAchievement(user, CHALLENGE_ACHV_NAME);
    }

    @ProtobufMessage(messageCase = MessageCase.REVOKECHALLENGE)
    private void revokeChallenge(final UserIdHolder user) {
        logger.log(Level.FINER, "Received revoke challenge from: {0}", user);

        synchronized (challenges) {
            for (Iterator<ChallengeData> it = challenges.iterator(); it.hasNext();) {
                ChallengeData data = it.next();
                if (data.challenger.equals(user)) {
                    sendRevokeMessage(data.challenged, user.getUsername());
                    it.remove();
                    break;
                }
            }
        }
    }

    private void sendRevokeMessage(final UserIdHolder sendTo, final String from) {
        communicationService.send(sendTo, WrapperMessage.newBuilder()
                .setRevokeChallenge(GameProtos.RevokeChallenge.newBuilder().setChallengeFrom(from))
                .build());
    }

    private void removeFromChallenges(final UserIdHolder user) {
        logger.log(Level.FINER, "Removing {0} from challenges", user);

        synchronized (challenges) {
            for (Iterator<ChallengeData> it = challenges.iterator(); it.hasNext();) {
                ChallengeData data = it.next();
                if (data.challenger.equals(user)) {
                    sendRevokeMessage(data.challenged, data.challenger.getUsername());
                    it.remove();
                } else if (data.challenged.equals(user)) {
                    communicationService.send(data.challenger, getChallengeNotAcceptedMsg(data.challenger.getUsername()));
                    it.remove();
                }
            }
        }
    }

    @Override
    public void onUserStateChanged(final UserIdHolder user, final Status newState) {
        if (newState != Status.MAIN_MENU) {
            removeFromChallenges(user);
        }
    }

    private static class ChallengeData {

        private final UserIdHolder challenger;
        private final FruitonTeam challengerTeam;
        private final UserIdHolder challenged;
        private final GameMode gameMode;
        private final PickMode pickMode;

        ChallengeData(
                final UserIdHolder challenger,
                final FruitonTeam challengerTeam,
                final UserIdHolder challenged,
                final GameMode mode,
                final PickMode pickMode
        ) {
            this.challenger = challenger;
            this.challengerTeam = challengerTeam;
            this.challenged = challenged;
            this.gameMode = mode;
            this.pickMode = pickMode;
        }
    }

}
