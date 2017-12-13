package cz.cuni.mff.fruiton.controller.web;

import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.service.authentication.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public final class IndexController {

    private final AuthenticationService authService;

    private final ServletContext context;

    @Autowired
    public IndexController(final AuthenticationService authService, final ServletContext context) {
        this.authService = authService;
        this.context = context;
    }

    /**
     * Shows login page.
     */
    @RequestMapping({"/", "/index"})
    public String index() {
        return "login";
    }

    @RequestMapping("/api/loginGoogleWeb")
    public void loginGoogle(
            final HttpServletRequest request,
            final HttpServletResponse response,
            @RequestParam final String idToken
    ) throws IOException {
        User u = authService.authenticate(idToken);
        if (u != null) {
            authService.createAuthenticatedSession(u, request);
            response.sendRedirect(context.getContextPath() + "/home");
        } else {
            response.sendRedirect(context.getContextPath() + "/registerGoogle?token=" + idToken);
        }
    }

}
