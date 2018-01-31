package cz.cuni.mff.fruiton.component;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.service.authentication.TokenService;
import cz.cuni.mff.fruiton.service.communication.SessionService;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public final class TokenAuthenticationFilter extends GenericFilterBean {

    public static final String AUTH_TOKEN_KEY = "x-auth-token";

    private final TokenService tokenService;
    private final SessionService sessionService;

    @Autowired
    public TokenAuthenticationFilter(final TokenService tokenService, final SessionService sessionService) {
        this.tokenService = tokenService;
        this.sessionService = sessionService;
    }

    @Override
    public void doFilter(
            final ServletRequest servletRequest,
            final ServletResponse servletResponse,
            final FilterChain filterChain
    ) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;

        boolean authorized = false;
        String token = request.getHeader(AUTH_TOKEN_KEY);
        if (token != null) {
            final UserIdHolder user = tokenService.getUser(token);
            if (user != null) {
                authorize(user);
                authorized = true;
            }
        }

        if (!authorized) {
            // token can be send as a param because Unity does not support opening browser with custom headers
            token = request.getParameter(AUTH_TOKEN_KEY);

            if (token != null) {
                final UserIdHolder user = tokenService.getUser(token);
                if (user != null
                        && sessionService.getSession(user).getRemoteAddress().getAddress().getHostAddress().equals(
                        servletRequest.getRemoteAddr())) { // check for the same address to improve security
                    authorize(user);
                    redirectWithoutAuthTokenParam((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse);
                    return;
                }
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private void authorize(final UserIdHolder user) {
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void redirectWithoutAuthTokenParam(final HttpServletRequest request, final HttpServletResponse response)
            throws IOException {
        String paramsString = URLEncodedUtils.format(request.getParameterMap().entrySet().stream()
                .filter(e -> !AUTH_TOKEN_KEY.equals(e.getKey()))
                .flatMap(e -> {
                            Stream.Builder<NameValuePair> params = Stream.builder();
                            for (String paramVal : e.getValue()) {
                                params.add(new BasicNameValuePair(e.getKey(), paramVal));
                            }

                            return params.build();
                        }).collect(Collectors.toList()), Charset.defaultCharset());

        if (!paramsString.isEmpty()) {
            paramsString = "&" + paramsString;
        }

        response.sendRedirect(request.getRequestURI() + paramsString);
    }

}
