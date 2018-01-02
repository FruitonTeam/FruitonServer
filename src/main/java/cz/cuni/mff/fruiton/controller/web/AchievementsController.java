package cz.cuni.mff.fruiton.controller.web;

import cz.cuni.mff.fruiton.service.authentication.AuthenticationService;
import cz.cuni.mff.fruiton.service.game.AchievementService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public final class AchievementsController {

    private final AchievementService achievementService;

    private final AuthenticationService authService;

    public AchievementsController(final AchievementService achievementService, final AuthenticationService authService) {
        this.achievementService = achievementService;
        this.authService = authService;
    }

    @RequestMapping(value = "/achievements")
    public String getAchievements(final Model model) {
        model.addAttribute("achievementStatuses", achievementService.getAchievementStatusesForUser(
                authService.getLoggedInUser()));
        return "achievements";
    }

}
