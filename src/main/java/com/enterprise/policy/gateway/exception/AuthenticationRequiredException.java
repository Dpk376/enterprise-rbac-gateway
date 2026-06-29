package com.enterprise.policy.gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AuthenticationRequiredException extends ResponseStatusException {
    
    public AuthenticationRequiredException(String reason) {
        super(HttpStatus.UNAUTHORIZED, reason);
    }
}
