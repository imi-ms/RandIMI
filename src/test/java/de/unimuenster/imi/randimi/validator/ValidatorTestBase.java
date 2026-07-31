package de.unimuenster.imi.randimi.validator;

import de.unimuenster.imi.randimi.RandimiIntegrationTest;
import de.unimuenster.imi.randimi.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * @author Daniel Preciado
 */
public abstract class ValidatorTestBase extends RandimiIntegrationTest {

    @Autowired
    protected MessageService messageService;

    protected String getMsg(String msgCode) {
        return getMsg(msgCode, new Object[]{});
    }

    protected String getMsg(String msgCode, Object... objects) {
        return messageService.getMessage(msgCode, objects);
    }
}
