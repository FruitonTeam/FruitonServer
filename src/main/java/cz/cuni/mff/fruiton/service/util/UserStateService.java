package cz.cuni.mff.fruiton.service.util;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.GameProtos.Status;

public interface UserStateService {

    interface OnUserStateChangedListener {

        void onUserStateChanged(UserIdHolder user, Status newState);

    }

    /**
     * Sets new state for specified users.
     * @param newState new state
     * @param users users for whom to set the new state
     */
    void setNewState(Status newState, UserIdHolder... users);

    /**
     * Returns state of the specified user.
     * @param user user whose state to fetch
     * @return state of the specified user
     */
    Status getState(UserIdHolder user);

    /**
     * Adds listener which is notified on any user's state changes.
     * @param listener listener to add
     */
    void addListener(OnUserStateChangedListener listener);

}
