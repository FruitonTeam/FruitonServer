package cz.cuni.mff.fruiton.controller.web;

import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dto.form.RegistrationForm;
import cz.cuni.mff.fruiton.service.authentication.AuthenticationService;
import cz.cuni.mff.fruiton.service.authentication.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

@Controller("webRegistrationController")
public final class RegistrationController {

    private final RegistrationService registrationService;
    private final AuthenticationService authService;

    private final ServletContext context;

    @Autowired
    public RegistrationController(
            final RegistrationService registrationService,
            final AuthenticationService authService,
            final ServletContext context
    ) {
        this.registrationService = registrationService;
        this.authService = authService;
        this.context = context;
    }

    @RequestMapping("/register")
    public String register(final Model model) {
        model.addAttribute("formData", new RegistrationForm());
        return "register";
    }

    @RequestMapping(value = "/registerWeb", method = RequestMethod.POST)
    public void registerWeb(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final @Valid RegistrationForm form
    ) throws IOException {
        User user = registrationService.register(form);
        authService.createAuthenticatedSession(user, request);

        response.sendRedirect(context.getContextPath() + "/home");
    }

}
