package cz.cuni.mff.fruiton.controller.api;

import cz.cuni.mff.fruiton.service.social.EmailConfirmationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public final class EmailConfirmationController {

    private final EmailConfirmationService emailConfirmationService;

    @Autowired
    public EmailConfirmationController(final EmailConfirmationService emailConfirmationService) {
        this.emailConfirmationService = emailConfirmationService;
    }

    @GetMapping(value = "/api/confirmMail")
    public String confirmMail(final Model model, @RequestParam(value = "confirmationId") final String confirmationId) {
        emailConfirmationService.confirmEmail(confirmationId);

        model.addAttribute("success", "Email successfully confirmed");

        return "login";
    }

}
