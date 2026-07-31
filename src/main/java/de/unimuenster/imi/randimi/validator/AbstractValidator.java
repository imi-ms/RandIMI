package de.unimuenster.imi.randimi.validator;

import de.unimuenster.imi.randimi.service.MessageService;
import org.springframework.validation.Validator;

public abstract class AbstractValidator implements Validator {

    protected final MessageService messageService;

    public AbstractValidator(final MessageService messageService) {
        this.messageService = messageService;
    }

    protected String getMsg(final String msgCode) {
        return getMsg(msgCode, new Object[]{});
    }

    protected String getMsg(final String msgCode, final Object... objects) {
        return messageService.getMessage(msgCode, objects);
    }
}
