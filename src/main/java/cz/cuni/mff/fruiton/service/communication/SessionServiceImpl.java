package cz.cuni.mff.fruiton.service.communication;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.net.InetAddress;
import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public final class SessionServiceImpl implements SessionService {

    private Map<Principal, WebSocketSession> sessions = new ConcurrentHashMap<>();

    private Map<InetAddress, Set<Principal>> playersOnTheSameAddressMap = new ConcurrentHashMap<>();

    @Override
    public void register(final WebSocketSession session) {
        Principal principal = session.getPrincipal();

        sessions.put(principal, session);
        Set<Principal> players = playersOnTheSameAddressMap.computeIfAbsent(session.getRemoteAddress().getAddress(),
                address -> Collections.synchronizedSet(new HashSet<>()));

        players.add(principal);
    }

    @Override
    public WebSocketSession getSession(final Principal principal) {
        return sessions.get(principal);
    }

    @Override
    public void unregister(final WebSocketSession session) {
        sessions.remove(session.getPrincipal());

        Set<Principal> players = playersOnTheSameAddressMap.get(session.getRemoteAddress().getAddress());
        if (players.size() == 1) {
            playersOnTheSameAddressMap.remove(session.getRemoteAddress().getAddress());
        } else {
            players.remove(session.getPrincipal());
        }
    }

    @Override
    public boolean hasOtherPlayersOnTheSameNetwork(final WebSocketSession session) {
        return playersOnTheSameAddressMap.get(session.getRemoteAddress().getAddress()).size() > 1;
    }

    @Override
    public Set<WebSocketSession> getOtherSessionsOnTheSameNetwork(final WebSocketSession session) {
        Set<WebSocketSession> otherSameNetworkSessions = new HashSet<>();
        for (Principal principal : playersOnTheSameAddressMap.get(session.getRemoteAddress().getAddress())) {
            if (!principal.equals(session.getPrincipal())) {
                otherSameNetworkSessions.add(sessions.get(principal));
            }
        }
        return otherSameNetworkSessions;
    }

}
