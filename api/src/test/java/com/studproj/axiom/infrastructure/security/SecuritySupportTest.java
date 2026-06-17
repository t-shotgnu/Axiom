package com.studproj.axiom.infrastructure.security;

import com.studproj.axiom.domain.exception.AccessDeniedException;
import com.studproj.axiom.domain.exception.AuthenticationRequiredException;
import com.studproj.axiom.domain.model.User;
import com.studproj.axiom.domain.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecuritySupportTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatedUserDetailsExposeStableAccountState() {
        UUID userId = UUID.randomUUID();
        AuthenticatedUserDetails details = new AuthenticatedUserDetails(
                userId,
                "ada@example.com",
                "encoded",
                true,
                List.of(new SimpleGrantedAuthority("USER"))
        );

        assertThat(details.id()).isEqualTo(userId);
        assertThat(details.getUsername()).isEqualTo("ada@example.com");
        assertThat(details.getPassword()).isEqualTo("encoded");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.isAccountNonExpired()).isTrue();
        assertThat(details.isAccountNonLocked()).isTrue();
        assertThat(details.isCredentialsNonExpired()).isTrue();
        assertThat(details.getAuthorities()).extracting("authority").containsExactly("USER");
    }

    @Test
    void providerReturnsIdAndEmailForAuthenticatedUserDetailsPrincipal() {
        UUID userId = UUID.randomUUID();
        AuthenticatedUserDetails details = new AuthenticatedUserDetails(
                userId,
                "ada@example.com",
                "encoded",
                true,
                List.of(new SimpleGrantedAuthority("USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(details, null, details.getAuthorities())
        );

        AuthenticatedUserProviderImpl provider = new AuthenticatedUserProviderImpl();

        assertThat(provider.getAuthenticatedUserId()).isEqualTo(userId);
        assertThat(provider.getAuthenticatedUserEmail()).isEqualTo("ada@example.com");
    }

    @Test
    void providerRequiresAuthenticatedContext() {
        AuthenticatedUserProviderImpl provider = new AuthenticatedUserProviderImpl();

        assertThatThrownBy(provider::getAuthenticatedUserId)
                .isInstanceOf(AuthenticationRequiredException.class)
                .hasMessage("No authenticated user");
        assertThatThrownBy(provider::getAuthenticatedUserEmail)
                .isInstanceOf(AuthenticationRequiredException.class)
                .hasMessage("No authenticated user");
    }

    @Test
    void providerRejectsPrincipalsWithoutUserIds() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("ada@example.com", null, "USER")
        );

        AuthenticatedUserProviderImpl provider = new AuthenticatedUserProviderImpl();

        assertThatThrownBy(provider::getAuthenticatedUserId)
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Authenticated principal does not contain user id");
    }

    @Test
    void customUserDetailsServiceLoadsUsersByEmail() {
        UUID userId = UUID.randomUUID();
        UserRepository userRepository = mock(UserRepository.class);
        User user = User.builder()
                .id(userId)
                .userName("ada")
                .emailAddress("ada@example.com")
                .password("encoded")
                .createdOn(LocalDateTime.now())
                .active(true)
                .build();
        when(userRepository.findByEmail("ada@example.com")).thenReturn(Optional.of(user));

        CustomUserDetailsService service = new CustomUserDetailsService(userRepository);

        AuthenticatedUserDetails details = (AuthenticatedUserDetails) service.loadUserByUsername("ada@example.com");

        assertThat(details.id()).isEqualTo(userId);
        assertThat(details.getUsername()).isEqualTo("ada@example.com");
        assertThat(details.getPassword()).isEqualTo("encoded");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.getAuthorities()).extracting("authority").containsExactly("USER");
    }

    @Test
    void customUserDetailsServiceThrowsWhenUserIsMissing() {
        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        CustomUserDetailsService service = new CustomUserDetailsService(userRepository);

        assertThatThrownBy(() -> service.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");
    }
}
