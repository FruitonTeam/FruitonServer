package cz.cuni.mff.fruiton.controller.web;

import cz.cuni.mff.fruiton.component.util.ReleasesHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public final class ReleaseController {

    private final ReleasesHelper releasesHelper;

    @Autowired
    public ReleaseController(final ReleasesHelper releasesHelper) {
        this.releasesHelper = releasesHelper;
    }

    @RequestMapping("/download")
    public String releases(final Model model) {
        model.addAttribute("releases", releasesHelper.getReleases());
        return "download";
    }

}
