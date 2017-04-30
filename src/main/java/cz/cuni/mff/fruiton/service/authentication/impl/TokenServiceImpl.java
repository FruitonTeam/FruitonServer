package cz.cuni.mff.fruiton.service.authentication.impl;

import cz.cuni.mff.fruiton.dao.model.User;
import cz.cuni.mff.fruiton.service.authentication.TokenService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class TokenServiceImpl implements TokenService {

    private static final Logger logger = Logger.getLogger(TokenServiceImpl.class.getName());

    private static final int INVALIDATION_TIME = 300_000;

    private final Map<String, TokenValue> tokens = Collections.synchronizedMap(new HashMap<>());

    @Override
    public void register(String token, User user) {
        TokenValue value = new TokenValue(user, Instant.now());
        tokens.put(token, value);
    }

    @Override
    public User getUserAndInvalidateToken(String token) {
        TokenValue value = tokens.get(token);
        if (value == null) {
            return null;
        }

        tokens.remove(token);

        return value.user;
    }

    @Override
    public boolean isValid(String token) {
        return tokens.containsKey(token);
    }

    @Scheduled(fixedDelay = INVALIDATION_TIME)
    public void invalidateTokens() {
        logger.finest("Invalidating token data");

        Instant now = Instant.now();

        synchronized (tokens) {
            for (Iterator<Map.Entry<String, TokenValue>> it = tokens.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, TokenValue> entry = it.next();

                TokenValue val = entry.getValue();

                if (now.minus(Duration.ofMillis(INVALIDATION_TIME)).isAfter(val.inserted)) {
                    it.remove();
                }
            }
        }

    }

    private static class TokenValue {

        private User user;
        private Instant inserted;

        public TokenValue(User user, Instant inserted) {
            this.user = user;
            this.inserted = inserted;
        }
    }

}