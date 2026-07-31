package de.unimuenster.imi.randimi.exceptions;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class ApiUserLoginException extends UsernameNotFoundException {
	public ApiUserLoginException(String msg) {
		super(msg);
	}
}
