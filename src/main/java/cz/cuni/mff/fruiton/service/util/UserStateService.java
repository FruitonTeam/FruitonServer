package cz.cuni.mff.fruiton.service.util;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.GameProtos.Status;

public interface UserStateService {

    interface OnUserStateChangedListener {

        void onUserStateChanged(UserIdHolder user, Status newState);

    }

    void setNewState(Status newState, UserIdHolder... users);

    Status getState(UserIdHolder user);

    void addListener(OnUserStateChangedListener listener);

}
