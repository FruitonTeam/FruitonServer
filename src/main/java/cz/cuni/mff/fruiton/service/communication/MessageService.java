package cz.cuni.mff.fruiton.service.communication;

import cz.cuni.mff.fruiton.dto.UserProtos;

import java.security.Principal;

public interface MessageService {

    void send(Principal principal, UserProtos.WrapperMessage message);

}
