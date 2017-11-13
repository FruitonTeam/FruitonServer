package cz.cuni.mff.fruiton.controller.web;

import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dto.form.EditProfileForm;
import cz.cuni.mff.fruiton.service.social.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@Controller
public class SettingsController {

    private final UserService userService;

    @Autowired
    public SettingsController(final UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "/settings", method = RequestMethod.GET)
    public final String profileInfo(final Model model) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        model.addAttribute("user", user);
        model.addAttribute("editProfileForm", EditProfileForm.parseFrom(user));
        return "settings";
    }

    @RequestMapping(value = "/settings/edit", method = RequestMethod.POST)
    public final String edit(
            @RequestHeader(value = "referer", required = false) final String referer,
            @Valid @ModelAttribute final EditProfileForm form
    ) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();


        if (form.getAvatar() != null && !form.getAvatar().isEmpty()) {
            // user changed his avatar
            userService.changeAvatar(user, form.getAvatar());
        }

        if (form.getEmail() != null && !form.getEmail().isEmpty() && !form.getEmail().equals(user.getEmail())) {
            // user changed his email
            userService.changeEmail(user, form.getEmail());
        }

        if (form.getPassword() != null && !form.getPassword().isEmpty()) {
            // user changed his password
            userService.changePassword(user, form.getPassword());
        }

        return "redirect:" + referer; // TODO: if referer is null then redirect to home page
    }

    @RequestMapping(value = "/settings/removeAvatar")
    public final String removeAvatar(
            @RequestHeader(value = "referer", required = false) final String referer,
            @RequestParam final String id
    ) {
        User user = userService.findUser(id);
        userService.changeAvatar(user, (MultipartFile) null);

        return "redirect:" + referer; // TODO: if referer is null then redirect to home page
    }

}
