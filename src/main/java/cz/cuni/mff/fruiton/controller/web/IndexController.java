package cz.cuni.mff.fruiton.controller.web;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.form.RenewPasswordForm;
import cz.cuni.mff.fruiton.service.authentication.AuthenticationService;
import cz.cuni.mff.fruiton.service.authentication.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

@Controller
public final class IndexController {

    private final AuthenticationService authService;

    private final PasswordService passwordService;

    private final ServletContext context;

    @Autowired
    public IndexController(
            final AuthenticationService authService,
            final ServletContext context,
            final PasswordService passwordService
    ) {
        this.authService = authService;
        this.context = context;
        this.passwordService = passwordService;
    }

    /**
     * Shows login page.
     */
    @RequestMapping({"/", "/index"})
    public String index() {
        return "login";
    }

    @RequestMapping("/loginFail")
    public String loginFail(final Model model) {
        model.addAttribute("showRenewPasswordInfo", true);
        return "login";
    }

    @RequestMapping("/api/loginGoogleWeb")
    public void loginGoogle(
            final HttpServletRequest request,
            final HttpServletResponse response,
            @RequestParam final String idToken
    ) throws IOException {
        UserIdHolder u = authService.authenticate(idToken);
        if (u != null) {
            authService.createAuthenticatedSession(u, request);
            response.sendRedirect(context.getContextPath() + "/home");
        } else {
            response.sendRedirect(context.getContextPath() + "/registerGoogle?token=" + idToken);
        }
    }

    @GetMapping("/renewPassword")
    public String renewPassword(final Model model) {
        model.addAttribute("form", new RenewPasswordForm());

        return "renewPassword";
    }

    @PostMapping("/renewPassword")
    public String renewPassword(@Valid final RenewPasswordForm form) {
        passwordService.renew(form.getEmail());

        return "redirect:/";
    }

}
