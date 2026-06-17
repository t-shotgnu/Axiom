package com.studproj.axiom.infrastructure.config;

import com.studproj.axiom.infrastructure.security.JwtFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import software.amazon.awssdk.services.s3.S3Client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class InfrastructureConfigTest {

    @Test
    void s3StorageConfigBuildsAnS3ClientFromProperties() {
        S3StorageConfig config = new S3StorageConfig();
        S3StorageProperties properties = new S3StorageProperties(
                "http://localhost:9000",
                "us-east-1",
                "axiom-test",
                "access",
                "secret"
        );

        try (S3Client client = config.s3Client(properties)) {
            assertThat(client.serviceName()).isEqualTo("s3");
        }
    }

    @Test
    void securityConfigProvidesPasswordEncoderAndCorsPolicy() {
        SecurityConfig config = new SecurityConfig(mock(JwtFilter.class));

        var passwordEncoder = config.passwordEncoder();
        String encoded = passwordEncoder.encode("secret");

        assertThat(encoded).isNotEqualTo("secret");
        assertThat(passwordEncoder.matches("secret", encoded)).isTrue();

        var request = new MockHttpServletRequest("OPTIONS", "/api/projects");
        var cors = config.corsConfigurationSource().getCorsConfiguration(request);

        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedOriginPatterns()).containsExactly("*");
        assertThat(cors.getAllowedMethods()).containsExactly("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
        assertThat(cors.getAllowedHeaders()).containsExactly("*");
        assertThat(cors.getAllowCredentials()).isTrue();
    }
}
