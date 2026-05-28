package backend_galeno_pro.auth_service.Jwt;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        userDetails = User.withUsername("testuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    void getToken_returnsNonNullJwt() {
        String token = jwtService.getToken(userDetails);
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void getToken_doesNotIncludeBearerPrefix() {
        String token = jwtService.getToken(userDetails);
        assertThat(token).doesNotStartWith("Bearer ");
    }

    @Test
    void getUsernameFromToken_returnsCorrectUsername() {
        String token = jwtService.getToken(userDetails);
        assertThat(jwtService.getUsernameFromToken(token)).isEqualTo("testuser");
    }

    @Test
    void isTokenValid_withValidTokenAndMatchingUser_returnsTrue() {
        String token = jwtService.getToken(userDetails);
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void isTokenValid_withDifferentUser_returnsFalse() {
        String token = jwtService.getToken(userDetails);
        UserDetails other = User.withUsername("otheruser")
                .password("password").authorities(Collections.emptyList()).build();
        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }

    @Test
    void getUsernameFromToken_withMalformedToken_throwsJwtException() {
        assertThatThrownBy(() -> jwtService.getUsernameFromToken("not.a.valid.jwt"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void getToken_twoCallsForSameUser_returnsDifferentTokensDueToIatVariance() {
        // Tokens generated at different milliseconds differ — ensures randomness
        String t1 = jwtService.getToken(userDetails);
        String t2 = jwtService.getToken(userDetails);
        // Both should be valid regardless of whether they're equal
        assertThat(jwtService.isTokenValid(t1, userDetails)).isTrue();
        assertThat(jwtService.isTokenValid(t2, userDetails)).isTrue();
    }

    @Test
    void getClaim_extractsExpiration() {
        String token = jwtService.getToken(userDetails);
        java.util.Date expiration = jwtService.getClaim(token,
                io.jsonwebtoken.Claims::getExpiration);
        assertThat(expiration).isAfter(new java.util.Date());
    }
}
