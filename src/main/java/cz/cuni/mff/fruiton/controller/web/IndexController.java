package cz.cuni.mff.fruiton.controller.web;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dto.form.ResetPasswordForm;
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
import java.util.Optional;

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
        model.addAttribute("showResetPasswordInfo", true);
        return "login";
    }

    @RequestMapping("/api/loginGoogleWeb")
    public void loginGoogle(
            final HttpServletRequest request,
            final HttpServletResponse response,
            @RequestParam final String idToken
    ) throws IOException {
        Optional<UserIdHolder> u = authService.authenticate(idToken);
        if (u.isPresent()) {
            authService.createAuthenticatedSession(u.get(), request);
            response.sendRedirect(context.getContextPath() + "/home");
        } else {
            response.sendRedirect(context.getContextPath() + "/registerGoogle?token=" + idToken);
        }
    }

    @GetMapping("/resetPassword")
    public String resetPassword(final Model model) {
        model.addAttribute("form", new ResetPasswordForm());

        return "resetPassword";
    }

    @PostMapping("/resetPassword")
    public String resetPassword(@Valid final ResetPasswordForm form) {
        passwordService.reset(form.getEmail());

        return "redirect:/";
    }

}
