package cz.cuni.mff.fruiton.config;

import cz.cuni.mff.fruiton.component.ProtobufWebSocketHandler;
import cz.cuni.mff.fruiton.service.authentication.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    public static final String TOKEN_HEADER_KEY = "x-auth-token";

    public static final String HTTP_SESSION_KEY = "session";

    private static final long IDLE_TIMEOUT = 2 * 60 * 1000; // 2 min

    private static final int HTTP_SESSION_INFINITE_INACTIVE_TIME = -1;

    private static final Logger logger = Logger.getLogger(WebSocketConfig.class.getName());

    private final TokenService tokenService;
    private final ProtobufWebSocketHandler handler;

    @Autowired
    public WebSocketConfig(final TokenService tokenService, final ProtobufWebSocketHandler handler) {
        this.tokenService = tokenService;
        this.handler = handler;
    }

    @Override
    public final void registerWebSocketHandlers(final WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/socket")
                .addInterceptors(websocketInterceptor())
                .setHandshakeHandler(defaultHandshakeHandler());
    }

    @Bean
    public DefaultHandshakeHandler defaultHandshakeHandler() {
        return new DefaultHandshakeHandler() {

            @Override
            protected Principal determineUser(
                    final ServerHttpRequest request,
                    final WebSocketHandler wsHandler,
                    final Map<String, Object> attributes
            ) {
                return (Principal) ((UsernamePasswordAuthenticationToken) request.getPrincipal()).getPrincipal();
            }

        };
    }

    @Bean
    public HandshakeInterceptor websocketInterceptor() {
        return new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(
                    final ServerHttpRequest serverHttpRequest,
                    final ServerHttpResponse serverHttpResponse,
                    final WebSocketHandler webSocketHandler,
                    final Map<String, Object> attributes
            ) {
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
                attributes.put(TOKEN_HEADER_KEY, token);

                HttpSession session = ((ServletServerHttpRequest) serverHttpRequest).getServletRequest().getSession();

                session.setMaxInactiveInterval(HTTP_SESSION_INFINITE_INACTIVE_TIME);

                attributes.put(HTTP_SESSION_KEY, session);

                return tokenService.isValid(token);
            }

            @Override
            public void afterHandshake(
                    final ServerHttpRequest serverHttpRequest,
                    final ServerHttpResponse serverHttpResponse,
                    final WebSocketHandler webSocketHandler,
                    final Exception e
            ) {

            }

        };
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxSessionIdleTimeout(IDLE_TIMEOUT);
        return container;
    }

}
