package cz.cuni.mff.fruiton.service.authentication.impl;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.service.authentication.TokenService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public final class TokenServiceImpl implements TokenService {

    private static final Logger logger = Logger.getLogger(TokenServiceImpl.class.getName());

    private static final int INVALIDATION_TIME = 3_600_000; // 1 hour

    private final Map<String, TokenValue> tokens = new ConcurrentHashMap<>();

    @Override
    public String register(final UserIdHolder user) {
        TokenValue value = new TokenValue(user, Instant.now());
        String token = generateToken();

        tokens.put(token, value);
        return token;
    }

    private String generateToken() {
        // theoretically we can generate the same token for 2 users but the probability is so small in comparison
        // with the synchronization overhead
        String token = UUID.randomUUID().toString();
        while (tokens.containsKey(token)) {
            token = UUID.randomUUID().toString();
        }
        return token;
    }

    @Override
    public void unregister(final UserIdHolder userToUnregister) {
        tokens.entrySet().removeIf(entry -> entry.getValue().user.equals(userToUnregister));
    }

    @Override
    public UserIdHolder getUser(final String token) {
        TokenValue value = tokens.get(token);
        if (value == null) {
            return null;
        }
        return value.user;
    }

    @Override
    public boolean isValid(final String token) {
        return tokens.containsKey(token);
    }

    @Override
    public void prolongLease(final String token) {
        TokenValue tokenValue = tokens.get(token);
        if (tokenValue != null) {
            tokenValue.inserted = Instant.now();
        } else {
            logger.log(Level.WARNING, "Trying to prolong lease on non existing token: {0}", token);
        }
    }

    @Scheduled(fixedDelay = INVALIDATION_TIME)
    public void invalidateTokens() {
        logger.finest("Invalidating token data");

        Instant now = Instant.now();

        tokens.entrySet().removeIf(entry -> now.minus(Duration.ofMillis(INVALIDATION_TIME)).isAfter(entry.getValue().inserted));
    }

    private static class TokenValue {

        private UserIdHolder user;
        private Instant inserted;

        TokenValue(final UserIdHolder user, final Instant inserted) {
            this.user = user;
            this.inserted = inserted;
        }
    }

}
