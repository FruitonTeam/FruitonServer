package cz.cuni.mff.fruiton.controller.api;

import cz.cuni.mff.fruiton.dao.domain.FruitonTeam;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.service.authentication.AuthenticationService;
import cz.cuni.mff.fruiton.service.game.PlayerService;
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

    private final PlayerService playerService;

    private final AuthenticationService authService;

    @Autowired
    public FruitonTeamController(final PlayerService playerService, final AuthenticationService authService) {
        this.playerService = playerService;
        this.authService = authService;
    }

    @RequestMapping(value = "/api/secured/addFruitonTeam", method = RequestMethod.POST)
    public final void saveFruitonTeam(@RequestBody final GameProtos.FruitonTeam team) {
        User user = authService.getLoggedInUser();
        playerService.addTeam(user, FruitonTeam.fromProtobuf(team));
    }

    @RequestMapping(value = "/api/secured/getAllFruitonTeams", produces = MediaTypes.PROTOBOUF)
    public final GameProtos.FruitonTeamList getFruitonTeams() {
        User user = authService.getLoggedInUser();

        List<GameProtos.FruitonTeam> teams = user.getTeams().stream().map(FruitonTeam::toProtobuf).collect(Collectors.toList());

        return GameProtos.FruitonTeamList.newBuilder()
                .addAllFruitonTeams(teams)
                .build();
    }

    @RequestMapping("/api/secured/removeFruitonTeam")
    public final void removeFruitonTeam(@RequestParam final String teamName) {
        User user = authService.getLoggedInUser();
        playerService.removeTeam(user, teamName);
    }

}
