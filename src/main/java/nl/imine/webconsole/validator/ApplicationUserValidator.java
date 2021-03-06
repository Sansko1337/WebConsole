package nl.imine.webconsole.validator;

import nl.imine.webconsole.model.ApplicationUser;
import nl.imine.webconsole.repository.ApplicationUserRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class ApplicationUserValidator implements Validator {

    private final ApplicationUserRepository applicationUserRepository;

    public ApplicationUserValidator(ApplicationUserRepository applicationUserRepository) {
        this.applicationUserRepository = applicationUserRepository;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return ApplicationUser.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ApplicationUser applicationUser = (ApplicationUser) target;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "username.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "password.empty");

        if(applicationUserRepository.exists(applicationUser.getUsername())) {
            errors.rejectValue("username", "username.exists");
        }
    }
}
