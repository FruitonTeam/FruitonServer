package cz.cuni.mff.fruiton.service.util;

import cz.cuni.mff.fruiton.dao.UserIdHolder;

public interface UserStateService {

    enum UserState {
        MAIN_MENU, IN_MATCHMAKING, IN_BATTLE, OFFLINE
    }

    interface OnUserStateChangedListener {

        void onUserStateChanged(UserIdHolder user, UserState newState);

    }

    void setNewState(UserState newState, UserIdHolder... users);

    UserState getState(UserIdHolder user);

    void addListener(OnUserStateChangedListener listener);

}
