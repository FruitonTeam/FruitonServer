package cz.cuni.mff.fruiton.service.communication;

import cz.cuni.mff.fruiton.dto.GameProtos;

import java.security.Principal;

public interface MessageService {

    void send(Principal principal, GameProtos.WrapperMessage message);

}
