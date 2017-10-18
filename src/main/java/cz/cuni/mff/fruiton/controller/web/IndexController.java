package cz.cuni.mff.fruiton.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {

    /**
     * Shows login page.
     */
    @RequestMapping({"/", "/index"})
    public String index() {
        return "login";
    }

}
