package de.unimuenster.imi.randimi.controller.api;

import de.unimuenster.imi.randimi.controller.ControllerTestBase;
import de.unimuenster.imi.randimi.service.auth.RandimiUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

/**
 * @author Daniel Preciado-Marquez
 */
@AutoConfigureMockMvc
@WithUserDetails(value = "api_test_user",
                 userDetailsServiceBeanName = RandimiUserDetailsService.USER_DETAILS_SERVICE_NAME)
public abstract class APIControllerTestBase extends ControllerTestBase {

    @Autowired
    protected MockMvc mockMvc;

}
