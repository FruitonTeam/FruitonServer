package cz.cuni.mff.fruiton.service.communication;

import cz.cuni.mff.fruiton.dto.CommonProtos.WrapperMessage;

import java.security.Principal;

public interface CommunicationService {

    void send(Principal principal, WrapperMessage message);

}