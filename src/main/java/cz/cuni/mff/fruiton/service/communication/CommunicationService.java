package cz.cuni.mff.fruiton.service.communication;

import cz.cuni.mff.fruiton.dto.CommonProtos.WrapperMessage;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.security.Principal;

public interface CommunicationService {

    void send(Principal principal, WrapperMessage message);

    void send(WebSocketSession session, WrapperMessage message);

    void send(WebSocketSession session, BinaryMessage message);

    void sendToAllContacts(Principal from, WrapperMessage message);

    void sendNotification(Principal principal, String base64Image, String header, String text);

}
