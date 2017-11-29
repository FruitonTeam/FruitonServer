package cz.cuni.mff.fruiton.controller.api;

import cz.cuni.mff.fruiton.dao.repository.UserRepository;
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

    @Autowired
    public PlayerController(final UserRepository repository, final PlayerService playerService) {
        this.repository = repository;
        this.playerService = playerService;
    }

    @RequestMapping("/api/player/exists")
    public boolean exists(@RequestParam final String login) {
        return repository.findByLogin(login) != null;
    }

    @RequestMapping("/api/player/isLoginValid")
    public ResponseEntity<Void> isLoginValid(@RequestParam final String login) {
        return respondOkIfNull(repository.findByLogin(login));
    }

    @RequestMapping("/api/player/isEmailValid")
    public ResponseEntity<Void> isEmailValid(@RequestParam final String email) {
        return respondOkIfNull(repository.findByEmail(email));
    }

    private ResponseEntity<Void> respondOkIfNull(final Object o) {
        return o == null ? ResponseEntity.ok(null) : new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
    }

    @RequestMapping("/api/player/availableFruitons")
    public List<Integer> getAvailableFruitons(@RequestParam final String login) {
        return playerService.getAvailableFruitons(login);
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
