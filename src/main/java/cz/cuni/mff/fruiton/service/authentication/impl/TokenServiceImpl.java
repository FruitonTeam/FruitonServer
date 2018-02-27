package cz.cuni.mff.fruiton.service.authentication.impl;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.service.authentication.TokenService;
import cz.cuni.mff.fruiton.service.communication.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public final class TokenServiceImpl implements TokenService {

    private static final Logger logger = Logger.getLogger(TokenServiceImpl.class.getName());

    private static final int INVALIDATION_TIME = 120_000; // 2 min

    private static final int TOKEN_VALID_PERIOD = 300_000; // 5 min

    private final Map<String, TokenValue> tokens = new HashMap<>();

    private final Set<UserIdHolder> users = new HashSet<>();

    private final ReadWriteLock tokensLock = new ReentrantReadWriteLock();

    private final SessionService sessionService;

    @Autowired
    public TokenServiceImpl(final SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public String register(final UserIdHolder user) {
        TokenValue value = new TokenValue(user, Instant.now());

        String token;
        try {
            tokensLock.writeLock().lock();

            if (users.contains(user)) {
                removeToken(user);
            } else {
                users.add(user);
            }

            token = generateToken();
            tokens.put(token, value);
        } finally {
            tokensLock.writeLock().unlock();
        }

        return token;
    }

    private void removeToken(final UserIdHolder user) {
        logger.log(Level.FINE, "Removing token explicitly for user {0}", user);
        try {
            tokensLock.writeLock().lock();
            for (Iterator<Map.Entry<String, TokenValue>> it = tokens.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, TokenValue> entry = it.next();
                if (entry.getValue().user.equals(user)) {
                    it.remove();
                    break;
                }
            }
        } finally {
            tokensLock.writeLock().unlock();
        }
    }

    private String generateToken() {
        String token = UUID.randomUUID().toString();
        while (tokens.containsKey(token)) {
            token = UUID.randomUUID().toString();
        }
        return token;
    }

    @Override
    public UserIdHolder getUser(final String token) {
        TokenValue value;
        try {
            tokensLock.readLock().lock();
            value = tokens.get(token);
        } finally {
            tokensLock.readLock().unlock();
        }
        if (value == null) {
            return null;
        }
        return value.user;
    }

    @Override
    public boolean isValid(final String token) {
        try {
            tokensLock.readLock().lock();
            return tokens.containsKey(token);
        } finally {
            tokensLock.readLock().unlock();
        }
    }

    @Scheduled(fixedDelay = INVALIDATION_TIME)
    public void invalidateTokens() {
        logger.finest("Invalidating token data");

        Instant lastValid = Instant.now().minus(Duration.ofMillis(TOKEN_VALID_PERIOD));

        try {
            tokensLock.writeLock().lock();
            for (Iterator<Map.Entry<String, TokenValue>> it = tokens.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, TokenValue> entry = it.next();

                TokenValue tokenValue = entry.getValue();
                if (lastValid.isAfter(tokenValue.validFrom)) {
                    if (sessionService.isOnline(tokenValue.user)) { // prolong lease
                        tokenValue.validFrom = Instant.now();
                    } else {
                        it.remove();
                        users.remove(tokenValue.user);
                    }
                }
            }
        } finally {
            tokensLock.writeLock().unlock();
        }
    }

    private static class TokenValue {

        private final UserIdHolder user;
        private Instant validFrom;

        TokenValue(final UserIdHolder user, final Instant validFrom) {
            this.user = user;
            this.validFrom = validFrom;
        }
    }

}
