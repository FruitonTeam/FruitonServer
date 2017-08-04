package cz.cuni.mff.fruiton.service.communication;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionServiceImpl implements SessionService {

    private Map<Principal, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public final void register(final WebSocketSession session) {
        sessions.put(session.getPrincipal(), session);
    }

    @Override
    public final WebSocketSession getSession(final Principal principal) {
        return sessions.get(principal);
    }

    @Override
    public final void unregister(final WebSocketSession session) {
        sessions.remove(session.getPrincipal());
    }

}
