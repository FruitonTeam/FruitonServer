package cz.cuni.mff.fruiton.controller.api;

import cz.cuni.mff.fruiton.dao.domain.FruitonTeam;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.service.game.PlayerService;
import cz.cuni.mff.fruiton.service.social.UserService;
import cz.cuni.mff.fruiton.web.MediaTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class FruitonTeamController {

    private final UserService userService;

    private final PlayerService playerService;

    @Autowired
    public FruitonTeamController(final UserService userService, final PlayerService playerService) {
        this.userService = userService;
        this.playerService = playerService;
    }

    @RequestMapping(value = "/api/addFruitonTeam", method = RequestMethod.POST)
    public final void saveFruitonTeam(@RequestBody final GameProtos.FruitonTeam team, @RequestParam final String login) {
        playerService.addTeam(userService.findUserByLogin(login), FruitonTeam.fromProtobuf(team));
    }

    @RequestMapping(value = "/api/getAllFruitonTeams", produces = MediaTypes.PROTOBOUF)
    public final GameProtos.FruitonTeamList getFruitonTeams(@RequestParam final String login) {
        User user = userService.findUserByLogin(login);

        List<GameProtos.FruitonTeam> teams = user.getTeams().stream().map(FruitonTeam::toProtobuf).collect(Collectors.toList());

        return GameProtos.FruitonTeamList.newBuilder()
                .addAllFruitonTeams(teams)
                .build();
    }

    @RequestMapping("/api/removeFruitonTeam")
    public final void removeFruitonTeam(@RequestParam final String login, @RequestParam final String teamName) {
        playerService.removeTeam(userService.findUserByLogin(login), teamName);
    }

}
