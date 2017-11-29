package cz.cuni.mff.fruiton.service.authentication.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.service.authentication.AuthenticationService;
import cz.cuni.mff.fruiton.service.authentication.RegistrationService;
import cz.cuni.mff.fruiton.service.social.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public final class AuthenticationServiceImpl implements AuthenticationService {

    private static final Logger logger = Logger.getLogger(AuthenticationServiceImpl.class.getName());

    private final UserRepository userRepository;

    private final GoogleIdTokenVerifier verifier;

    private final PasswordEncoder passwordEncoder;

    private final RegistrationService registrationService;

    private final UserService userService;

    @Autowired
    public AuthenticationServiceImpl(
            final UserRepository userRepository,
            final GoogleIdTokenVerifier verifier,
            final PasswordEncoder passwordEncoder,
            final RegistrationService registrationService,
            final UserService userService
    ) {
        this.userRepository = userRepository;
        this.verifier = verifier;
        this.passwordEncoder = passwordEncoder;
        this.registrationService = registrationService;
        this.userService = userService;
    }

    @Override
    public User authenticate(final String login, final String password) {
        User user = userRepository.findByLogin(login);
        if (user == null) {
            throw new UsernameNotFoundException("User " + login + " is not registered.");
        }

        boolean authenticated = passwordEncoder.matches(password, user.getPassword());
        if (!authenticated) {
            throw new BadCredentialsException("Incorrect password");
        }

        return user;
    }

    @Override
    public User authenticate(final String idTokenStr) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenStr);

            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                User user = userRepository.findByGoogleSubject(payload.getSubject());
                if (user != null) { // user logged via google before
                    return user;
                }

                // first login
                final User registeredUser = registrationService.register(payload);
                getGooglePictureUrl(payload).ifPresentOrElse(url -> userService.changeAvatar(registeredUser, url),
                        () -> logger.log(Level.FINER,
                                "User {0} does not have google avatar, using default one", registeredUser));

                return registeredUser;
            }

        } catch (GeneralSecurityException e) {
            // probably ignore, google provides no documentation when this exception occurs
            logger.log(Level.FINE, "GeneralSecurityException while verifying google token", e);
        } catch (IOException e) {
            logger.log(Level.WARNING, "IOException occurred while verifying google token", e);
        }

        throw new AuthenticationServiceException("Could not verify google token");
    }

    private Optional<String> getGooglePictureUrl(final GoogleIdToken.Payload payload) {
        String pictureUrl = (String) payload.get("picture");
        if (pictureUrl != null) {
            return Optional.of(pictureUrl);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void createAuthenticatedSession(final User user, final HttpServletRequest request) {
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // add authentication to session
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());
    }

}
