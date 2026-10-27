package com.barrilli.agendadortarefas.infra.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private static final String SECRET = Base64.getEncoder().encodeToString("segredo-de-teste-com-pelo-menos-32-bytes!".getBytes());

    private final JwtUtil jwtUtil = comSegredo(SECRET);

    private static JwtUtil comSegredo(String secret) {
        JwtUtil util = new JwtUtil();
        ReflectionTestUtils.setField(util, "secretKey", secret);
        return util;
    }

    private static String token(String secret, String email, long expiraEmMs) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiraEmMs))
                .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret)))
                .compact();
    }

    @Test
    void tokenValido_extraiUsuarioEValida() {
        String token = token(SECRET, "ana@t.com", 60_000);

        assertThat(jwtUtil.extractUsername(token)).isEqualTo("ana@t.com");
        assertThat(jwtUtil.validateToken(token, "ana@t.com")).isTrue();
        assertThat(jwtUtil.validateToken(token, "outro@t.com")).isFalse();
    }

    @Test
    void tokenExpirado_eRejeitado() {
        String token = token(SECRET, "ana@t.com", -1_000);

        assertThatThrownBy(() -> jwtUtil.extractUsername(token)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void tokenAssinadoComOutroSegredo_eRejeitado() {
        String outroSegredo = Base64.getEncoder().encodeToString("outro-segredo-de-teste-com-32-bytes!!".getBytes());
        String token = token(outroSegredo, "ana@t.com", 60_000);

        assertThatThrownBy(() -> jwtUtil.extractUsername(token)).isInstanceOf(SignatureException.class);
    }
}
