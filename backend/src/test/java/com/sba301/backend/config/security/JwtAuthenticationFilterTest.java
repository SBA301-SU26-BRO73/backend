package com.sba301.backend.config.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.sba301.backend.service.JwtService;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validAccessToken_setsUserDetailsImplPrincipalWithId() throws Exception {
        String token = "valid.jwt.token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractTokenType(token)).thenReturn("access_token");
        when(jwtService.extractEmail(token)).thenReturn("staff@test.com");
        when(jwtService.extractRole(token)).thenReturn("STAFF");
        when(jwtService.extractUserId(token)).thenReturn(42L);

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        // The crux of the fix: principal is a UserDetailsImpl, so @AuthenticationPrincipal resolves.
        assertThat(auth.getPrincipal()).isInstanceOf(UserDetailsImpl.class);
        assertThat(((UserDetailsImpl) auth.getPrincipal()).getId()).isEqualTo(42L);
        // Backward compatibility for other APIs: getName() stays the email, authority stays ROLE_<role>.
        assertThat(auth.getName()).isEqualTo("staff@test.com");
        assertThat(auth.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_STAFF");
    }

    @Test
    void noAuthorizationHeader_leavesContextEmpty() throws Exception {
        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
