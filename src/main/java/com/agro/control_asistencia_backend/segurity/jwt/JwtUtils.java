package com.agro.control_asistencia_backend.segurity.jwt;

import java.security.Key;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.agro.control_asistencia_backend.segurity.service.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);


    @Value("${agro.app.jwtSecret}")
    private String jwtSecret;

    @Value("${agro.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    
    public String generateJwtToken(Authentication authentication) {
        
        
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject((userPrincipal.getUsername())) 
                .claim("roles", userPrincipal.getAuthorities().iterator().next().getAuthority()) 
                .setIssuedAt(new Date()) 
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs)) 
                .signWith(key(), SignatureAlgorithm.HS256) 
                .compact(); 
    }
    
    
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }


    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            // Intenta parsear y verificar el token con la clave secreta
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
