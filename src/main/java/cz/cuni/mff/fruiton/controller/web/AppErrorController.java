package cz.cuni.mff.fruiton.controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * Controller for errors that were not caught by {@link org.springframework.web.bind.annotation.ExceptionHandler}
 * annotation.
 */
@Controller
public class AppErrorController implements ErrorController {

    private static final String ERROR_PATH = "/error";

    private final ErrorAttributes errorAttributes;

    @Autowired
    public AppErrorController(final ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    /**
     * Shows user nice web page with error message.
     */
    @RequestMapping(value = ERROR_PATH)
    public String handleError(final HttpServletRequest request, final Model model) {
        Throwable t = errorAttributes.getError(new ServletWebRequest(request));
        if (t != null) {
            model.addAttribute("message", t.getLocalizedMessage());
        } else {
            model.addAttribute("message", "Unspecified error");
        }
        return "error";
    }

    /** {@inheritDoc} */
    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }

}
