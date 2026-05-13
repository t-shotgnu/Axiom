package com.studproj.axiom.infrastructure.security;

import com.studproj.axiom.domain.exception.ForbiddenException;
import com.studproj.axiom.domain.exception.UnauthorizedException;
import com.studproj.axiom.domain.service.CurrentUserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CurrentUserServiceImpl implements CurrentUserService {
    @Override
    public UUID getUserId() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("No authenticated user");
        }

        var principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUserDetails userDetails) {
            return userDetails.id();
        }

        throw new ForbiddenException("Authenticated principal does not contain user id");
    }
}
