package cz.cuni.mff.fruiton.service.communication;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.net.InetAddress;
import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public final class SessionServiceImpl implements SessionService {

    private static final Logger logger = Logger.getLogger(SessionServiceImpl.class.getName());

    private Map<Principal, WebSocketSession> sessions = new HashMap<>();

    private final ReadWriteLock sessionsLock = new ReentrantReadWriteLock();

    private Map<InetAddress, Set<Principal>> playersOnTheSameAddressMap = new HashMap<>();

    private final ReadWriteLock playersOnTheSameAddressLock = new ReentrantReadWriteLock();

    @Override
    public void register(final WebSocketSession session) {
        Principal principal = session.getPrincipal();

        try {
            sessionsLock.writeLock().lock();
            playersOnTheSameAddressLock.writeLock().lock();

            sessions.put(principal, session);
            Set<Principal> players = playersOnTheSameAddressMap.computeIfAbsent(session.getRemoteAddress().getAddress(),
                    address -> new HashSet<>());

            players.add(principal);
        } finally {
            sessionsLock.writeLock().unlock();
            playersOnTheSameAddressLock.writeLock().unlock();
        }
    }

    @Override
    public WebSocketSession getSession(final Principal principal) {
        try {
            sessionsLock.readLock().lock();
            return sessions.get(principal);
        } finally {
            sessionsLock.readLock().unlock();
        }
    }

    @Override
    public void unregister(final WebSocketSession session) {
        try {
            sessionsLock.writeLock().lock();
            playersOnTheSameAddressLock.writeLock().lock();

            if (!sessions.containsKey(session.getPrincipal())) {
                logger.log(Level.WARNING, "Trying to unregister unknown session {0}", session);
                return;
            }

            sessions.remove(session.getPrincipal());

            Set<Principal> players = playersOnTheSameAddressMap.get(session.getRemoteAddress().getAddress());
            if (players.size() == 1) {
                playersOnTheSameAddressMap.remove(session.getRemoteAddress().getAddress());
            } else {
                players.remove(session.getPrincipal());
            }
        } finally {
            sessionsLock.writeLock().unlock();
            playersOnTheSameAddressLock.writeLock().unlock();
        }
    }

    @Override
    public boolean hasOtherPlayersOnTheSameNetwork(final WebSocketSession session) {
        try {
            playersOnTheSameAddressLock.readLock().lock();

            Set<Principal> otherPlayersOnNetwork = playersOnTheSameAddressMap.get(session.getRemoteAddress().getAddress());
            return otherPlayersOnNetwork != null && otherPlayersOnNetwork.size() > 1;
        } finally {
            playersOnTheSameAddressLock.readLock().unlock();
        }
    }

    @Override
    public Set<WebSocketSession> getOtherSessionsOnTheSameNetwork(final WebSocketSession session) {
        try {
            playersOnTheSameAddressLock.readLock().lock();

            Set<WebSocketSession> otherSameNetworkSessions = new HashSet<>();
            Set<Principal> otherPlayers = playersOnTheSameAddressMap.get(session.getRemoteAddress().getAddress());
            if (otherPlayers != null) {
                for (Principal principal : otherPlayers) {
                    if (!principal.equals(session.getPrincipal())) {
                        otherSameNetworkSessions.add(sessions.get(principal));
                    }
                }
            }
            return otherSameNetworkSessions;
        } finally {
            playersOnTheSameAddressLock.readLock().unlock();
        }
    }

    @Override
    public boolean isOnline(final Principal principal) {
        try {
            sessionsLock.readLock().lock();
            return sessions.containsKey(principal);
        } finally {
            sessionsLock.readLock().unlock();
        }
    }

}
