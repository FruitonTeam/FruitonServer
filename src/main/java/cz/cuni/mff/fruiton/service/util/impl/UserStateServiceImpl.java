package cz.cuni.mff.fruiton.service.util.impl;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.CommonProtos;
import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.dto.GameProtos.Status;
import cz.cuni.mff.fruiton.service.communication.CommunicationService;
import cz.cuni.mff.fruiton.service.communication.SessionService;
import cz.cuni.mff.fruiton.service.util.UserStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public final class UserStateServiceImpl implements UserStateService {

    private static final Logger logger = Logger.getLogger(UserStateServiceImpl.class.getName());

    private Map<UserIdHolder, Status> states = new ConcurrentHashMap<>();

    private final SessionService sessionService;

    private final CommunicationService communicationService;

    private final List<OnUserStateChangedListener> onUserStateChangedListeners = new LinkedList<>();

    @Autowired
    public UserStateServiceImpl(
            final SessionService sessionService,
            final CommunicationService communicationService
    ) {
        this.sessionService = sessionService;
        this.communicationService = communicationService;
    }

    @Override
    public void setNewState(final Status newState, final UserIdHolder... users) {
        if (users == null) {
            throw new IllegalArgumentException("Cannot change state for null users");
        }
        if (newState == null) {
            throw new IllegalArgumentException("Cannot change state to null");
        }

        logger.log(Level.FINEST, "Changing state of users {0} to {1}", new Object[] {Arrays.toString(users), newState});

        for (UserIdHolder user : users) {
            if (getState(user) == newState) { // state was not changed
                continue;
            }

            if (newState == Status.OFFLINE) {
                states.remove(user);
            } else {
                if (!sessionService.isOnline(user)) {
                    logger.log(Level.WARNING, "Cannot change state of offline user");
                    continue;
                }
                states.put(user, newState);
            }
            communicationService.sendToAllContacts(user, CommonProtos.WrapperMessage.newBuilder()
                    .setStatusChange(GameProtos.StatusChange.newBuilder()
                            .setLogin(user.getUsername())
                            .setStatus(newState))
                    .build());

            synchronized (onUserStateChangedListeners) {
                for (OnUserStateChangedListener listener : onUserStateChangedListeners) {
                    listener.onUserStateChanged(user, newState);
                }
            }
        }
    }

    @Override
    public Status getState(final UserIdHolder user) {
        if (user == null) {
            throw new IllegalArgumentException("Cannot get state for null user");
        }
        Status status = states.get(user);
        if (status == null) {
            return Status.OFFLINE;
        }
        return status;
    }

    @Override
    public void addListener(final OnUserStateChangedListener listener) {
        synchronized (onUserStateChangedListeners) {
            onUserStateChangedListeners.add(listener);
        }
    }

}
