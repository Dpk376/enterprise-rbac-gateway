package com.enterprise.policy.gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AuthorizationDeniedException extends ResponseStatusException {
    
    public AuthorizationDeniedException(String reason) {
        super(HttpStatus.FORBIDDEN, reason);
    }
}
