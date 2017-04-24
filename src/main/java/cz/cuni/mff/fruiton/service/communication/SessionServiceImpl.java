package cz.cuni.mff.fruiton.service.communication;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Service
public class SessionServiceImpl implements SessionService {

    private Map<Principal, WebSocketSession> sessions = new HashMap<>();

    @Override
    public void register(WebSocketSession session) {
        sessions.put(session.getPrincipal(), session);
    }

    @Override
    public WebSocketSession getSession(Principal principal) {
        return sessions.get(principal);
    }

    @Override
    public void unregister(WebSocketSession session) {
        sessions.remove(session.getPrincipal());
    }

}
