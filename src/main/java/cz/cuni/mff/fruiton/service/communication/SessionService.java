package cz.cuni.mff.fruiton.service.communication;

import org.springframework.web.socket.WebSocketSession;

import java.security.Principal;
import java.util.Set;

public interface SessionService {

    void register(WebSocketSession session);

    WebSocketSession getSession(Principal principal);

    void unregister(WebSocketSession session);

    boolean hasOtherPlayersOnTheSameNetwork(WebSocketSession session);

    Set<WebSocketSession> getOtherSessionsOnTheSameNetwork(WebSocketSession session);

    boolean isOnline(Principal principal);

}
