package cz.cuni.mff.fruiton.controller.api;

import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.service.authentication.AuthenticationService;
import cz.cuni.mff.fruiton.service.communication.SessionService;
import cz.cuni.mff.fruiton.service.communication.chat.FriendshipService;
import cz.cuni.mff.fruiton.service.social.UserService;
import cz.cuni.mff.fruiton.service.util.UserStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public final class PlayerController {

    private final UserRepository repository;

    private final UserService userService;

    private final AuthenticationService authService;

    private final FriendshipService friendshipService;

    private final SessionService sessionService;

    private final UserStateService userStateService;

    @Autowired
    public PlayerController(
            final UserRepository repository,
            final UserService userService,
            final AuthenticationService authService,
            final FriendshipService friendshipService,
            final SessionService sessionService,
            final UserStateService userStateService
    ) {
        this.repository = repository;
        this.userService = userService;
        this.authService = authService;
        this.friendshipService = friendshipService;
        this.sessionService = sessionService;
        this.userStateService = userStateService;
    }

    @RequestMapping("/api/player/exists")
    public boolean exists(@RequestParam final String login) {
        return repository.findByLogin(login) != null;
    }

    @RequestMapping("/api/player/isLoginAvailable")
    public ResponseEntity<Void> isLoginAvailable(@RequestParam final String login) {
        return repository.existsByLogin(login) ? new ResponseEntity<>(HttpStatus.BAD_REQUEST) : ResponseEntity.ok(null);
    }

    @RequestMapping("/api/player/isLoginUsed")
    public ResponseEntity<Void> isLoginUsed(@RequestParam("username") final String login) {
        return repository.existsByLogin(login) ? ResponseEntity.ok(null) : new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping("/api/player/isEmailAvailable")
    public ResponseEntity<Void> isEmailAvailable(@RequestParam final String email) {
        return repository.existsByEmail(email) ? new ResponseEntity<>(HttpStatus.BAD_REQUEST) : ResponseEntity.ok(null);
    }

    @RequestMapping("/api/player/isEmailUsed")
    public ResponseEntity<Void> isEmailUsed(@RequestParam final String email) {
        return repository.existsByEmail(email) ? ResponseEntity.ok(null) : new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping("/api/secured/player/availableFruitons")
    public List<Integer> getAvailableFruitons() {
        return userService.getAvailableFruitons(authService.getLoggedInUser());
    }

    @GetMapping("/api/secured/player/fruitonsAvailableForSelling")
    public List<Integer> getFruitonsAvailableForSelling() {
        return userService.getFruitonsAvailableForSelling(authService.getLoggedInUser()).stream()
                .map(fruiton -> fruiton.dbId).collect(Collectors.toList());
    }

    @RequestMapping("/api/player/avatar")
    public ResponseEntity<String> getAvatar(@RequestParam final String login) {
        Optional<String> encodedAvatar = userService.getBase64Avatar(login);
        if (encodedAvatar.isPresent()) {
            return new ResponseEntity<>(encodedAvatar.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @RequestMapping("/api/secured/player/friendRequests")
    public List<String> getAllFriendRequests() {
        return friendshipService.getAllFriendRequests(authService.getLoggedInUser());
    }

    @ResponseBody
    @GetMapping("/api/player/isOnline")
    public boolean isOnline(@RequestParam final String login) {
        return sessionService.isOnline(userService.findUserByLogin(login));
    }

    @ResponseBody
    @GetMapping(value = "/api/player/status")
    public int getStatus(@RequestParam final String login) {
        return userStateService.getState(userService.findUserByLogin(login)).getNumber();
    }

}
