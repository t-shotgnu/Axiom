package com.studproj.axiom.infrastructure.config;

import com.studproj.axiom.domain.exception.AccessDeniedException;
import com.studproj.axiom.domain.exception.AuthenticationRequiredException;
import com.studproj.axiom.domain.exception.DomainRuleViolationException;
import com.studproj.axiom.domain.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/projects/project-1");

    @Test
    void mapsEntityNotFoundToProblemDetail() {
        var response = handler.handleNotFound(new EntityNotFoundException("Project not found"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Not Found");
        assertThat(response.getBody().getDetail()).isEqualTo("Project not found");
        assertThat(response.getBody().getInstance()).hasToString("/api/projects/project-1");
    }

    @Test
    void mapsDomainRuleViolationsToBadRequest() {
        var response = handler.handleDomainRuleViolation(new DomainRuleViolationException("Invalid transition"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Bad request");
        assertThat(response.getBody().getDetail()).isEqualTo("Invalid transition");
    }

    @Test
    void mapsAuthenticationRequiredToUnauthorized() {
        var response = handler.handleUnauthorized(new AuthenticationRequiredException("No authenticated user"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Unauthorized");
        assertThat(response.getBody().getDetail()).isEqualTo("No authenticated user");
    }

    @Test
    void mapsAccessDeniedToForbidden() {
        var response = handler.handleForbidden(new AccessDeniedException("No project access"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(403);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Forbidden");
        assertThat(response.getBody().getDetail()).isEqualTo("No project access");
    }

    @Test
    void mapsLargeUploadsToBadRequestWithConfiguredSizeHint() {
        var response = handler.handleMaxUploadSizeExceeded(
                new MaxUploadSizeExceededException(10 * 1024 * 1024),
                request
        );

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("File too large");
        assertThat(response.getBody().getDetail()).contains("Maximum allowed file size is 10 MB.");
    }
}
