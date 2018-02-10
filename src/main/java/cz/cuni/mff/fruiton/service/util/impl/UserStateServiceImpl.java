package cz.cuni.mff.fruiton.service.util.impl;

import cz.cuni.mff.fruiton.component.util.OnDisconnectedListener;
import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.service.communication.SessionService;
import cz.cuni.mff.fruiton.service.util.UserStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public final class UserStateServiceImpl implements UserStateService, OnDisconnectedListener {

    private static final Logger logger = Logger.getLogger(UserStateServiceImpl.class.getName());

    private Map<UserIdHolder, UserState> states = new ConcurrentHashMap<>();

    private final SessionService sessionService;

    private final List<OnUserStateChangedListener> onUserStateChangedListeners = new LinkedList<>();

    @Autowired
    public UserStateServiceImpl(final SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public void setNewState(final UserState newState, final UserIdHolder... users) {
        if (users == null) {
            throw new IllegalArgumentException("Cannot change state for null users");
        }
        if (newState == null) {
            throw new IllegalArgumentException("Cannot change state to null");
        }

        logger.log(Level.FINEST, "Changing state of users {0} to {1}", new Object[] {users, newState});

        for (UserIdHolder user : users) {
            if (!sessionService.isOnline(user)) {
                logger.log(Level.WARNING, "Cannot change state of offline user");
                continue;
            }

            states.put(user, newState);

            synchronized (onUserStateChangedListeners) {
                for (OnUserStateChangedListener listener : onUserStateChangedListeners) {
                    listener.onUserStateChanged(user, newState);
                }
            }
        }
    }

    @Override
    public UserState getState(final UserIdHolder user) {
        if (user == null) {
            throw new IllegalArgumentException("Cannot get state for null user");
        }
        if (!sessionService.isOnline(user)) {
            return UserState.OFFLINE;
        }
        return states.computeIfAbsent(user, key -> UserState.MAIN_MENU);
    }

    @Override
    public void addListener(final OnUserStateChangedListener listener) {
        synchronized (onUserStateChangedListeners) {
            onUserStateChangedListeners.add(listener);
        }
    }

    @Override
    public void onDisconnected(final UserIdHolder user) {
        states.remove(user);
    }

}
