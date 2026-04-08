package com.barrilli.agendadortarefas.infra.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtUtil {

    private final SecretKey secretKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        byte[] key = Base64.getDecoder().decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(key); // Chave fornecida via ambiente/propriedade
    }

    private SecretKey getSecretKey() {
        return secretKey;
    }

    // Extrai as claims do token JWT (informacoes adicionais do token)
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey()) // Define a chave secreta para validar a assinatura do token
                .build()
                .parseSignedClaims(token) // Analisa o token JWT e obtem as claims
                .getPayload(); // Retorna o corpo das claims
    }

    // Extrai o nome de usuario do token JWT
    public String extractUsername(String token) {
        // Obtem o assunto (nome de usuario) das claims do token
        return extractClaims(token).getSubject();
    }

    // Verifica se o token JWT esta expirado
    public boolean isTokenExpired(String token) {
        // Compara a data de expiracao do token com a data atual
        return extractClaims(token).getExpiration().before(new Date());
    }

    // Valida o token JWT verificando o nome de usuario e se o token nao esta expirado
    public boolean validateToken(String token, String username) {
        // Extrai o nome de usuario do token
        final String extractedUsername = extractUsername(token);
        // Verifica se o nome de usuario do token corresponde ao fornecido e se o token nao esta expirado
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }
}
