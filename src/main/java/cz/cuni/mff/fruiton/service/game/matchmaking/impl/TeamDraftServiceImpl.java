package cz.cuni.mff.fruiton.service.game.matchmaking.impl;

import cz.cuni.mff.fruiton.annotation.ProtobufMessage;
import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.CommonProtos;
import cz.cuni.mff.fruiton.dto.CommonProtos.WrapperMessage;
import cz.cuni.mff.fruiton.dto.CommonProtos.WrapperMessage.MessageCase;
import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.dto.GameProtos.DraftRequest;
import cz.cuni.mff.fruiton.dto.GameProtos.DraftResponse;
import cz.cuni.mff.fruiton.dto.GameProtos.DraftResult;
import cz.cuni.mff.fruiton.dto.GameProtos.FruitonTeam;
import cz.cuni.mff.fruiton.dto.GameProtos.FruitonType;
import cz.cuni.mff.fruiton.dto.GameProtos.GameMode;
import cz.cuni.mff.fruiton.dto.GameProtos.GameOver.Reason;
import cz.cuni.mff.fruiton.dto.GameProtos.GameRewards;
import cz.cuni.mff.fruiton.dto.GameProtos.Position;
import cz.cuni.mff.fruiton.dto.GameProtos.Status;
import cz.cuni.mff.fruiton.service.communication.CommunicationService;
import cz.cuni.mff.fruiton.service.game.FruitonService;
import cz.cuni.mff.fruiton.service.game.GameService;
import cz.cuni.mff.fruiton.service.game.matchmaking.TeamDraftService;
import cz.cuni.mff.fruiton.service.social.UserService;
import cz.cuni.mff.fruiton.service.util.UserStateService;
import cz.cuni.mff.fruiton.service.util.UserStateService.OnUserStateChangedListener;
import cz.cuni.mff.fruiton.util.FruitonTeamUtils;
import cz.cuni.mff.fruiton.util.KernelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public final class TeamDraftServiceImpl implements TeamDraftService, OnUserStateChangedListener {

    private static final int DRAFT_CHECK_TIME_REFRESH_TIME = 1000;

    private static final int PICK_TIME = 15; // in seconds

    private final Map<UserIdHolder, TeamDraftPicker> draftPickers = new HashMap<>();

    private final ReadWriteLock draftPickersLock = new ReentrantReadWriteLock();

    private final Random random = new Random();

    private final CommunicationService communicationService;

    private final GameService gameService;

    private final UserStateService userStateService;

    private final UserService userService;

    private final FruitonService fruitonService;

    @Autowired
    public TeamDraftServiceImpl(
            final CommunicationService communicationService,
            final GameService gameService,
            final UserStateService userStateService,
            final UserService userService,
            final FruitonService fruitonService
    ) {
        this.communicationService = communicationService;
        this.gameService = gameService;
        this.userStateService = userStateService;
        this.userService = userService;
        this.fruitonService = fruitonService;
    }

    @PostConstruct
    private void init() {
        userStateService.addListener(this);
    }

    @Override
    public void startDraft(final UserIdHolder user1, final UserIdHolder user2, final GameMode gameMode) {
        boolean firstUserPicks = random.nextBoolean();

        TeamDraftPicker picker = new TeamDraftPicker(user1, user2, gameMode, firstUserPicks);

        try {
            draftPickersLock.writeLock().lock();
            draftPickers.put(user1, picker);
            draftPickers.put(user2, picker);

            synchronized (picker.lock) {
                sendDraftReadyMessages(picker);
                picker.sendRequest(communicationService);
            }

            userStateService.setNewState(Status.IN_MATCHMAKING, user1, user2);

        } finally {
            draftPickersLock.writeLock().unlock();
        }
    }

    private void sendDraftReadyMessages(final TeamDraftPicker picker) {
        sendDraftReadyMessage(picker.user1, userService.getProtobufPlayerInfo(picker.user2), picker.firstUserPicks);
        sendDraftReadyMessage(picker.user2, userService.getProtobufPlayerInfo(picker.user1), !picker.firstUserPicks);
    }

    private void sendDraftReadyMessage(
            final UserIdHolder toUser,
            final GameProtos.PlayerInfo opponentInfo,
            final boolean startsFirst
    ) {
        communicationService.send(toUser, WrapperMessage.newBuilder()
                .setDraftReady(GameProtos.DraftReady.newBuilder()
                        .setOpponent(opponentInfo)
                        .setStartsFirst(startsFirst))
                .build());
    }

    private void removeFromDraft(final UserIdHolder user, final Reason reason) {
        TeamDraftPicker picker = getPicker(user);
        if (picker != null) {
            UserIdHolder opponent = picker.getOpponent(user);
            sendGameOverMessage(opponent, reason, GameRewards.newBuilder().build(), opponent.getName());

            synchronized (picker.lock) {
                picker.setFinished();
            }

            userStateService.setNewState(Status.MAIN_MENU, opponent);
        }
    }

    private void sendGameOverMessage(
            final UserIdHolder to,
            final Reason reason,
            final GameRewards rewards,
            final String winnerLogin
    ) {
        communicationService.send(to, CommonProtos.WrapperMessage.newBuilder()
                .setGameOver(GameProtos.GameOver.newBuilder()
                        .setReason(reason)
                        .setGameRewards(rewards)
                        .setWinnerLogin(winnerLogin)
                        .build())
                .build());
    }

    @ProtobufMessage(messageCase = MessageCase.DRAFTRESPONSE)
    private void handleDraftResponse(final UserIdHolder user, final DraftResponse draftResponse) {
        if (!userService.getAvailableFruitons(user).contains(draftResponse.getFruitonId())) {
            throw new FruitonTeamUtils.NotUnlockedFruitonException(List.of(draftResponse.getFruitonId()));
        }

        TeamDraftPicker picker = getPicker(user);
        synchronized (picker.lock) {
            if (!picker.getPickingUser().equals(user)) {
                throw new IllegalStateException(user + " is not picking");
            }

            picker.addFruiton(draftResponse.getFruitonId(), communicationService);
            moveToNextOrFinish(picker);
        }
    }

    private TeamDraftPicker getPicker(final UserIdHolder user) {
        try {
            draftPickersLock.readLock().lock();
            return draftPickers.get(user);
        } finally {
            draftPickersLock.readLock().unlock();
        }
    }

    private void moveToNextOrFinish(final TeamDraftPicker picker) {
        if (picker.moveToNext()) {
            picker.sendRequest(communicationService);
        } else {
            picker.setFinished();
            gameService.createGame(picker.user1, picker.team1Builder.build(),
                    picker.user2, picker.team2Builder.build(), picker.gameMode);
        }
    }

    @ProtobufMessage(messageCase = MessageCase.DRAFTSURRENDERMESSAGE)
    private void handleDraftSurrenderMessage(final UserIdHolder user) {
        removeFromDraft(user, Reason.SURRENDER);
    }

    @Scheduled(fixedDelay = DRAFT_CHECK_TIME_REFRESH_TIME)
    private void checkDraftTime() {
        try {
            draftPickersLock.writeLock().lock();

            Instant now = Instant.now();

            Set<TeamDraftPicker> processed = new HashSet<>();
            for (Iterator<Entry<UserIdHolder, TeamDraftPicker>> it = draftPickers.entrySet().iterator(); it.hasNext();) {
                Entry<UserIdHolder, TeamDraftPicker> entry = it.next();

                TeamDraftPicker picker = entry.getValue();
                synchronized (picker.lock) {
                    if (picker.isFinished()) {
                        it.remove();
                        continue;
                    }
                    if (processed.contains(picker)) {
                        continue;
                    }

                    if (picker.requestTime.plusSeconds(picker.getActiveOption().pickTime).isBefore(now)) {
                        // pick random
                        List<Integer> availableFruitons = userService.getAvailableFruitons(picker.getPickingUser());
                        List<Integer> fruitonsWithCorrectType = fruitonService.filter(availableFruitons,
                                FruitonService.FruitonType.fromProtobuf(picker.getActiveOption().type));
                        picker.addFruiton(fruitonsWithCorrectType.get(random.nextInt(fruitonsWithCorrectType.size())),
                                communicationService);
                        moveToNextOrFinish(picker);
                    }

                    processed.add(picker);
                }
            }

        } finally {
            draftPickersLock.writeLock().unlock();
        }
    }

    @Override
    public void onUserStateChanged(final UserIdHolder user, final Status newState) {
        if (newState == Status.OFFLINE) {
            removeFromDraft(user, Reason.DISCONNECT);
        }
    }

    private static class TeamDraftPicker {

        private static class FruitonPickOption {

            private final Position position;
            private final FruitonType type;
            private int pickTime = PICK_TIME;

            FruitonPickOption(final Position position, final FruitonType type) {
                this.position = position;
                this.type = type;
            }

            FruitonPickOption(final Position position, final FruitonType type, final int pickTime) {
                this.position = position;
                this.type = type;
                this.pickTime = pickTime;
            }
        }

        private static final List<FruitonPickOption> PICK_OPTIONS = List.of(
                new FruitonPickOption(KernelUtils.positionOf(4, 0), FruitonType.KING),
                new FruitonPickOption(KernelUtils.positionOf(3, 0), FruitonType.MAJOR),
                new FruitonPickOption(KernelUtils.positionOf(5, 0), FruitonType.MAJOR),
                new FruitonPickOption(KernelUtils.positionOf(4, 1), FruitonType.MINOR),
                new FruitonPickOption(KernelUtils.positionOf(3, 1), FruitonType.MINOR),
                new FruitonPickOption(KernelUtils.positionOf(5, 1), FruitonType.MINOR),
                new FruitonPickOption(KernelUtils.positionOf(2, 0), FruitonType.MAJOR),
                new FruitonPickOption(KernelUtils.positionOf(6, 0), FruitonType.MAJOR),
                new FruitonPickOption(KernelUtils.positionOf(2, 1), FruitonType.MINOR),
                new FruitonPickOption(KernelUtils.positionOf(6, 1), FruitonType.MINOR)
        );

        private final UserIdHolder user1;
        private final UserIdHolder user2;

        private final GameMode gameMode;

        private boolean firstUserPicks;

        private Instant requestTime;

        private FruitonTeam.Builder team1Builder = FruitonTeam.newBuilder();
        private FruitonTeam.Builder team2Builder = FruitonTeam.newBuilder();

        private int activeOption = 0;

        private final Object lock = new Object();

        private boolean finished = false;

        private TeamDraftPicker(
                final UserIdHolder user1,
                final UserIdHolder user2,
                final GameMode gameMode,
                final boolean firstUserPicks
        ) {
            this.user1 = user1;
            this.user2 = user2;
            this.gameMode = gameMode;
            this.firstUserPicks = firstUserPicks;
        }

        private FruitonPickOption getActiveOption() {
            return PICK_OPTIONS.get(activeOption / 2);
        }

        private boolean isFinished() {
            return finished;
        }

        private void setFinished() {
            this.finished = true;
        }

        private UserIdHolder getPickingUser() {
            if (firstUserPicks) {
                return user1;
            } else {
                return user2;
            }
        }

        private FruitonTeam.Builder getPickingUserTeamBuilder() {
            if (firstUserPicks) {
                return team1Builder;
            } else {
                return team2Builder;
            }
        }

        private UserIdHolder getOpponent(final UserIdHolder user) {
            if (user.equals(user1)) {
                return user2;
            } else if (user.equals(user2)) {
                return user1;
            } else {
                throw new IllegalArgumentException("User " + user + " is not part of this draft");
            }
        }

        private void sendRequest(final CommunicationService communicationService) {
            if (finished) {
                throw new IllegalStateException("Cannot send request because picking is finished");
            }

            FruitonPickOption option = getActiveOption();

            DraftRequest request = DraftRequest.newBuilder()
                    .setFruitonType(option.type)
                    .setPosition(option.position)
                    .setSecondsToPick(option.pickTime)
                    .build();

            communicationService.send(getPickingUser(), WrapperMessage.newBuilder().setDraftRequest(request).build());

            requestTime = Instant.now();
        }

        private void addFruiton(final int fruitonId, final CommunicationService communicationService) {
            if (finished) {
                throw new IllegalStateException("Cannot send request because picking is finished");
            }

            FruitonPickOption option = getActiveOption();

            getPickingUserTeamBuilder().addFruitonIDs(fruitonId);
            getPickingUserTeamBuilder().addPositions(option.position);

            sendDraftResult(fruitonId, communicationService, option);
        }

        private void sendDraftResult(
                final int fruitonId,
                final CommunicationService communicationService,
                final FruitonPickOption option
        ) {
            WrapperMessage draftResult = WrapperMessage.newBuilder()
                    .setDraftResult(DraftResult.newBuilder()
                            .setLogin(getPickingUser().getUsername())
                            .setFruitonId(fruitonId)
                            .setPosition(option.position))
                    .build();

            communicationService.send(user1, draftResult);
            communicationService.send(user2, draftResult);
        }

        private boolean moveToNext() {
            if (finished) {
                throw new IllegalStateException("Cannot send request because picking is finished");
            }

            activeOption++;
            firstUserPicks = !firstUserPicks;

            return activeOption < PICK_OPTIONS.size() * 2;
        }

    }

}
