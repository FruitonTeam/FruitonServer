package cz.cuni.mff.fruiton.service.communication;

import org.springframework.web.socket.WebSocketSession;

import java.security.Principal;
import java.util.Set;

public interface SessionService {

    /**
     * Registers specified session for this service.
     * @param session session to register
     */
    void register(WebSocketSession session);

    /**
     * Looks up session for specified user.
     * @param principal user whose session to return
     * @return session of the specified user
     */
    WebSocketSession getSession(Principal principal);

    /**
     * Unregisters specified session from this service. This method should be called after
     * {@link #unregisterFromSameNetwork(WebSocketSession)}.
     * @param session session to unregister
     */
    void unregister(WebSocketSession session);

    /**
     * Unregisters session from the same network data.
     * @param session session to unregister
     */
    void unregisterFromSameNetwork(WebSocketSession session);

    /**
     * Determines whether any other players are on the same network as provided session.
     * @param session session from which network information is retrieved
     * @return true if there is more players on the same network, false otherwise
     */
    boolean hasOtherPlayersOnTheSameNetwork(WebSocketSession session);

    /**
     * Returns other sessions on the same network as provided session.
     * @param session session from which network information is retrieved
     * @return other sessions on the same network
     */
    Set<WebSocketSession> getOtherSessionsOnTheSameNetwork(WebSocketSession session);

    /**
     * Determines whether supplied user is online.
     * @param principal user for which to check online status
     * @return true if user has registered session, false otherwise
     */
    boolean isOnline(Principal principal);

}
