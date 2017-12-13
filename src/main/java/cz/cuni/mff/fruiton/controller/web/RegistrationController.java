package cz.cuni.mff.fruiton.controller.web;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dto.form.GoogleRegistrationForm;
import cz.cuni.mff.fruiton.dto.form.RegistrationForm;
import cz.cuni.mff.fruiton.service.authentication.AuthenticationService;
import cz.cuni.mff.fruiton.service.authentication.RegistrationService;
import cz.cuni.mff.fruiton.service.social.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

@Controller("webRegistrationController")
public final class RegistrationController {

    private final RegistrationService registrationService;
    private final AuthenticationService authService;
    private final UserService userService;

    private final ServletContext context;

    @Autowired
    public RegistrationController(
            final RegistrationService registrationService,
            final AuthenticationService authService,
            final ServletContext context,
            final UserService userService
    ) {
        this.registrationService = registrationService;
        this.authService = authService;
        this.context = context;
        this.userService = userService;
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

    @RequestMapping(value = "/registerGoogle", method = RequestMethod.GET)
    public String registerGoogle(final Model model, @RequestParam final String token) {

        GoogleIdToken.Payload p = authService.verify(token);

        model.addAttribute("regModel", new GoogleRegistrationForm(token, userService.generateRandomName(p)));

        return "registerGoogle";
    }

    @RequestMapping(value = "/registerGoogle", method = RequestMethod.POST)
    public void registerGoogle(
            final HttpServletRequest request,
            final HttpServletResponse response,
            @Valid final GoogleRegistrationForm form
    ) throws IOException {
        GoogleIdToken.Payload p = authService.verify(form.getIdToken());

        User user = registrationService.register(form.getLogin(), p);

        authService.createAuthenticatedSession(user, request);

        response.sendRedirect(context.getContextPath() + "/home");
    }

}
