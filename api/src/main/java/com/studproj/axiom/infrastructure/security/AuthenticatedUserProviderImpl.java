package com.studproj.axiom.infrastructure.security;

import com.studproj.axiom.domain.exception.AccessDeniedException;
import com.studproj.axiom.domain.exception.AuthenticationRequiredException;
import com.studproj.axiom.domain.service.AuthenticatedUserProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthenticatedUserProviderImpl implements AuthenticatedUserProvider {
    @Override
    public UUID getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationRequiredException("No authenticated user");
        }

        var principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUserDetails userDetails) {
            return userDetails.id();
        }

        throw new AccessDeniedException("Authenticated principal does not contain user id");
    }

    @Override
    public String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationRequiredException("No authenticated user");
        }

        return authentication.getName();
    }
}
