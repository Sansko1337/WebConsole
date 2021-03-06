package nl.imine.webconsole.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import nl.imine.webconsole.model.ApplicationUser;
import nl.imine.webconsole.repository.ApplicationUserRepository;
import nl.imine.webconsole.service.ApplicationUserService;
import nl.imine.webconsole.validator.ApplicationUserValidator;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/users")
public class UserPageController {

    private final ApplicationUserRepository applicationUserRepository;
    private final ApplicationUserService applicationUserService;
    private final ApplicationUserValidator applicationUserValidator;

    public UserPageController(ApplicationUserRepository applicationUserRepository, ApplicationUserService applicationUserService, ApplicationUserValidator applicationUserValidator) {
        this.applicationUserRepository = applicationUserRepository;
        this.applicationUserService = applicationUserService;
        this.applicationUserValidator = applicationUserValidator;
    }

    @GetMapping
    public ModelAndView getPage() throws JsonProcessingException {
        ModelAndView modelAndView = new ModelAndView("user/userList");
        modelAndView.addObject("users", cleanUsers(applicationUserRepository.findAll()));
        return modelAndView;
    }

    @GetMapping("/new")
    public ModelAndView getNewUserPage() {
        return new ModelAndView("user/newUser");
    }

    @PostMapping("/new")
    public ModelAndView createNewUser(NewApplicationUser applicationUser, BindingResult bindingResult) throws JsonProcessingException {
        applicationUserValidator.validate(applicationUser, bindingResult);

        if (!applicationUser.getPassword().equals(applicationUser.getNewPassword())) {
            bindingResult.rejectValue("newPassword", "password.not.equal");
        }

        if (bindingResult.hasErrors()) {
            return new ModelAndView("user/newUser", "errors", bindingResult.getAllErrors().stream().map(ObjectError::getCode).collect(Collectors.toList()));
        }

        //Recreate object as down-casted type so JPA can handle it. Also removes passwordConfirm object
        applicationUserService.save(new ApplicationUser(applicationUser.getUsername(), applicationUser.getPassword(), applicationUser.getRoles()));
        ModelAndView page = getPage();
        page.addObject("createdUser", cleanUser(applicationUser));
        return page;
    }

    private List<ApplicationUser> cleanUsers(List<ApplicationUser> users) {
        return users.stream()
                .peek(this::cleanUser)
                .collect(Collectors.toList());
    }

    private ApplicationUser cleanUser(ApplicationUser applicationUser) {
        applicationUser.setPassword("");
        return applicationUser;
    }

    private static class NewApplicationUser extends ApplicationUser {

        private String newPassword;

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}
