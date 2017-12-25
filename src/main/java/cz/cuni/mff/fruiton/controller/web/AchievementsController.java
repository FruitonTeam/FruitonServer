package cz.cuni.mff.fruiton.controller.web;

import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.service.authentication.AuthenticationService;
import cz.cuni.mff.fruiton.service.game.AchievementService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public final class AchievementsController {

    private final AuthenticationService authService;

    private final AchievementService achievementService;

    public AchievementsController(final AuthenticationService authService, final AchievementService achievementService) {
        this.authService = authService;
        this.achievementService = achievementService;
    }

    @RequestMapping(value = "/achievements")
    public String getAchievements(final Model model) {
        User user = authService.getLoggedInUser();

        model.addAttribute("achievementStatuses", achievementService.getAchievementStatusesForUser(user));
        return "achievements";
    }

}
