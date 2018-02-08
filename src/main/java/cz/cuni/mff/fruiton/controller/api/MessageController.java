package cz.cuni.mff.fruiton.controller.api;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.ChatProtos;
import cz.cuni.mff.fruiton.service.authentication.AuthenticationService;
import cz.cuni.mff.fruiton.service.communication.chat.ChatService;
import cz.cuni.mff.fruiton.service.social.UserService;
import cz.cuni.mff.fruiton.web.MediaTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public final class MessageController {

    private final UserService userService;
    private final AuthenticationService authService;
    private final ChatService chatService;

    @Autowired
    public MessageController(
            final UserService userService,
            final AuthenticationService authService,
            final ChatService chatService
    ) {
        this.userService = userService;
        this.authService = authService;
        this.chatService = chatService;
    }

    @GetMapping(value = "/api/secured/getAllMessagesWithUser", produces = MediaTypes.PROTOBOUF)
    public ChatProtos.ChatMessages getAllMessagesWithUser(
            @RequestParam("otherUserLogin") final String otherLogin,
            @RequestParam final int page
    ) {
        UserIdHolder user = authService.getLoggedInUser();
        UserIdHolder otherUser = userService.findUserByLogin(otherLogin);

        return chatService.getMessagesBetweenUsers(user, otherUser, page);
    }

    @GetMapping(value = "/api/secured/getAllMessagesBefore", produces = MediaTypes.PROTOBOUF)
    public ChatProtos.ChatMessages getAllMessagesBefore(
            @RequestParam final String messageId,
            @RequestParam final int page
    ) {
        return chatService.getMessagesBefore(messageId, page);
    }

}
