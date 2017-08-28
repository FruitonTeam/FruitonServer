package cz.cuni.mff.fruiton.service.communication;

import cz.cuni.mff.fruiton.dto.GameProtos;

import java.security.Principal;

public interface CommunicationService {

    void send(Principal principal, GameProtos.WrapperMessage message);

}
