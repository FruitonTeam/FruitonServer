package cz.cuni.mff.fruiton.service.communication;

import cz.cuni.mff.fruiton.dto.CommonProtos.WrapperMessage;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.security.Principal;

public interface CommunicationService {

    /**
     * Sends message to specified user.
     * @param principal user to whom to send the message
     * @param message message to send
     */
    void send(Principal principal, WrapperMessage message);

    /**
     * Sends message to specified session.
     * @param session session to which to send the message
     * @param message message to send
     */
    void send(WebSocketSession session, WrapperMessage message);

    /**
     * Sends message to specified session.
     * @param session session to which to send the message
     * @param message message to send
     */
    void send(WebSocketSession session, BinaryMessage message);

    /**
     * Sends message to all contacts. Contact is user who is a friend or who is on the same network.
     * @param from user for whose contacts to send the message
     * @param message message to send
     */
    void sendToAllContacts(Principal from, WrapperMessage message);

    /**
     * Sends notification to specified user.
     * @param principal user to whom to send the notification
     * @param base64Image image that will be shown in the notification encoded in base64
     * @param title notification title
     * @param text notification text
     */
    void sendNotification(Principal principal, String base64Image, String title, String text);

}
