package cz.cuni.mff.fruiton.config;

import cz.cuni.mff.fruiton.component.ProtobufWebSocketHandler;
import cz.cuni.mff.fruiton.service.authentication.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private static final String TOKEN_HEADER_KEY = "x-auth-token";

    private static final Logger logger = Logger.getLogger(WebSocketConfig.class.getName());

    private final TokenService tokenService;
    private final ProtobufWebSocketHandler handler;

    @Autowired
    public WebSocketConfig(TokenService tokenService, ProtobufWebSocketHandler handler) {
        this.tokenService = tokenService;
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/socket")
                .addInterceptors(websocketInterceptor())
                .setHandshakeHandler(new DefaultHandshakeHandler() {

                    @Override
                    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
                        String token = request.getHeaders().get(TOKEN_HEADER_KEY).get(0);
                        return tokenService.getUserAndInvalidateToken(token);
                    }

                });
    }

    @Bean
    public HandshakeInterceptor websocketInterceptor() {
        return new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest serverHttpRequest,
                                           ServerHttpResponse serverHttpResponse,
                                           WebSocketHandler webSocketHandler,
                                           Map<String, Object> map) throws Exception {

                List<String> tokens = serverHttpRequest.getHeaders().get(TOKEN_HEADER_KEY);
                if (tokens == null || tokens.isEmpty()) {
                    logger.log(Level.WARNING, "Received websocket handshake request without token from: {0}",
                            serverHttpRequest.getRemoteAddress());
                    serverHttpResponse.setStatusCode(HttpStatus.BAD_REQUEST);
                    return false;
                }

                if (tokens.size() != 1) {
                    logger.log(Level.WARNING, "Invalid count of token headers: {0}. We need exactly one.", tokens);
                    serverHttpResponse.setStatusCode(HttpStatus.BAD_REQUEST);
                    return false;
                }

                String token = tokens.get(0);

                return tokenService.isValid(token);
            }

            @Override
            public void afterHandshake(ServerHttpRequest serverHttpRequest,
                                       ServerHttpResponse serverHttpResponse,
                                       WebSocketHandler webSocketHandler,
                                       Exception e) {

            }

        };
    }

}
