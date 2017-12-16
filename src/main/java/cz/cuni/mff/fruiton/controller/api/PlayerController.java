package cz.cuni.mff.fruiton.controller.api;

import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.service.authentication.AuthenticationService;
import cz.cuni.mff.fruiton.service.game.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import java.io.IOException;
import java.util.Optional;

@RestController
public final class PlayerController {

    private final UserRepository repository;

    private final PlayerService playerService;

    private final AuthenticationService authService;

    @Autowired
    public PlayerController(
            final UserRepository repository,
            final PlayerService playerService,
            final AuthenticationService authService
    ) {
        this.repository = repository;
        this.playerService = playerService;
        this.authService = authService;
    }

    @RequestMapping("/api/player/exists")
    public boolean exists(@RequestParam final String login) {
        return repository.findByLogin(login) != null;
    }

    @RequestMapping("/api/player/isLoginAvailable")
    public ResponseEntity<Void> isLoginAvailable(@RequestParam final String login) {
        return repository.existsByLogin(login) ? new ResponseEntity<>(HttpStatus.BAD_REQUEST) : ResponseEntity.ok(null);
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
        User user = authService.getLoggedInUser();
        return playerService.getAvailableFruitons(user);
    }

    @RequestMapping("/api/player/avatar")
    public ResponseEntity<String> getAvatar(@RequestParam final String login) throws IOException {
        Optional<String> encodedAvatar = playerService.getBase64Avatar(login);
        if (encodedAvatar.isPresent()) {
            return new ResponseEntity<>(encodedAvatar.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
