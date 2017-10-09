package cz.cuni.mff.fruiton.controller.api;

import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.service.game.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @RequestMapping("/api/player/availableFruitons")
    public List<Integer> getAvailableFruitons(@RequestParam final String login) {
        return playerService.getAvailableFruitons(login);
    }

}
