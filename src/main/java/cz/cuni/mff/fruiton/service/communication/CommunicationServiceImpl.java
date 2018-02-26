package cz.cuni.mff.fruiton.service.communication;

import cz.cuni.mff.fruiton.dao.UserDAO;
import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.CommonProtos;
import cz.cuni.mff.fruiton.dto.CommonProtos.WrapperMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.security.Principal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public final class CommunicationServiceImpl implements CommunicationService {

    private static final Logger logger = Logger.getLogger(CommunicationServiceImpl.class.getName());

    private final SessionService sessionService;

    private final UserDAO userDAO;

    @Autowired
    public CommunicationServiceImpl(final SessionService sessionService, final UserDAO userDAO) {
        this.sessionService = sessionService;
        this.userDAO = userDAO;
    }

    @Override
    public void send(final Principal principal, final WrapperMessage message) {
        logger.log(Level.FINEST, "Sending message {0} to user {1}", new Object[] {message, principal});

        WebSocketSession session = sessionService.getSession(principal);
        if (session == null) {
            logger.log(Level.WARNING, "Cannot send message because connection was lost with {0}", principal);
            return;
        }
        send(session, getBinaryMessage(message));
    }

    private BinaryMessage getBinaryMessage(final WrapperMessage message) {
        return new BinaryMessage(message.toByteArray());
    }

    @Override
    public void send(final WebSocketSession session, final WrapperMessage message) {
        send(session, getBinaryMessage(message));
    }

    @Override
    public void send(final WebSocketSession session, final BinaryMessage message) {
        try {
            synchronized (session) { // sendMessage is not thread-safe
                session.sendMessage(message);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Cannot send websocket message", e);
        }
    }

    @Override
    public void sendToAllContacts(final Principal from, final WrapperMessage message) {
        logger.log(Level.FINEST, "Sending message {0} to all contacts of {1}", new Object[] {message, from});

        BinaryMessage msg = getBinaryMessage(message);
        for (WebSocketSession session : getAllOnlineContacts(from)) {
            send(session, msg);
        }
    }

    private Set<WebSocketSession> getAllOnlineContacts(final Principal principal) {
        Set<WebSocketSession> onlineContacts = new HashSet<>(
                sessionService.getOtherSessionsOnTheSameNetwork(sessionService.getSession(principal)));

        onlineContacts.addAll(userDAO.getFriends((UserIdHolder) principal).stream().map(sessionService::getSession)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));

        return onlineContacts;
    }

    @Override
    public void sendNotification(
            final Principal principal,
            final String base64Image,
            final String title,
            final String text
    ) {
        WrapperMessage notification = WrapperMessage.newBuilder()
                .setNotification(CommonProtos.Notification.newBuilder()
                        .setImage(base64Image)
                        .setTitle(title)
                        .setText(text)
                        .build())
                .build();

        send(principal, notification);
    }

}
