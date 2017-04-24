package cz.cuni.mff.fruiton.service.communication;

import org.springframework.web.socket.WebSocketSession;

import java.security.Principal;

public interface SessionService {

    void register(WebSocketSession session);

    WebSocketSession getSession(Principal principal);

    void unregister(WebSocketSession session);

}
