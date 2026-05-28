package backend_galeno_pro.auth_service.Jwt;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtService jwtService;
    @Mock private UserDetailsService userDetailsService;
    @InjectMocks private JwtAuthenticationFilter filter;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain chain;

    @BeforeEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void resetSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ── no token ──────────────────────────────────────────────────────────────

    @Test
    void doFilterInternal_noAuthHeader_chainsAndSkipsJwt() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    void doFilterInternal_noBearerPrefix_chainsAndSkipsJwt() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    void doFilterInternal_emptyAuthHeader_chainsAndSkipsJwt() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("");

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    // ── invalid token ─────────────────────────────────────────────────────────

    @Test
    void doFilterInternal_malformedToken_returns401AndDoesNotChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer bad.token");
        when(jwtService.getUsernameFromToken("bad.token"))
                .thenThrow(new JwtException("malformed"));
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        filter.doFilterInternal(request, response, chain);

        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verifyNoInteractions(chain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    // ── valid token ───────────────────────────────────────────────────────────

    @Test
    void doFilterInternal_validToken_setsAuthenticationAndChains() throws Exception {
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("testuser").password("pass")
                .authorities(Collections.emptyList()).build();

        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt");
        when(jwtService.getUsernameFromToken("valid.jwt")).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtService.isTokenValid("valid.jwt", userDetails)).thenReturn(true);

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo("testuser");
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_tokenFailsValidation_doesNotSetAuthButChains() throws Exception {
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("testuser").password("pass")
                .authorities(Collections.emptyList()).build();

        when(request.getHeader("Authorization")).thenReturn("Bearer expired.jwt");
        when(jwtService.getUsernameFromToken("expired.jwt")).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtService.isTokenValid("expired.jwt", userDetails)).thenReturn(false);

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_authAlreadySet_skipsUserDetailsLoadAndChains() throws Exception {
        // Pre-populate the security context so the filter's guard fires
        org.springframework.security.core.Authentication existing =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        "already", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(existing);

        when(request.getHeader("Authorization")).thenReturn("Bearer some.jwt");
        when(jwtService.getUsernameFromToken("some.jwt")).thenReturn("already");

        filter.doFilterInternal(request, response, chain);

        verifyNoInteractions(userDetailsService);
        verify(chain).doFilter(request, response);
    }
}
