package com.studproj.axiom.domain.service;

import java.util.UUID;

public interface AuthenticatedUserProvider {
    UUID getAuthenticatedUserId();

    String getAuthenticatedUserEmail();
}
