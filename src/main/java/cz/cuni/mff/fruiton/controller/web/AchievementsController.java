package cz.cuni.mff.fruiton.controller.web;

import cz.cuni.mff.fruiton.dao.repository.AchievementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public final class AchievementsController {

    private final AchievementRepository repository;

    @Autowired
    public AchievementsController(final AchievementRepository repository) {
        this.repository = repository;
    }

    @RequestMapping(value = "/achievements")
    public String getAchievements(final Model model) {
        model.addAttribute("achievements", repository.findAll());
        return "achievements";
    }

}
