package net.openwebinars.gateway.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${app.security.jwt.secret}")
    private String secret;

    private Key key;

    @PostConstruct
    public void init(){
        this.key = Keys.hmacShaKeyFor(secret.getBytes());


    }

    public Claims getAllClaimsFromToken(String token) {

        token= token.substring("Bearer".length());
        JwtParser parser = Jwts.parserBuilder().setSigningKey(key).build();
        JwsHeader<?> header = parser.parseClaimsJws(token).getHeader();

        if (!SignatureAlgorithm.HS512.getValue().equals(header.getAlgorithm())) {
            throw new IllegalArgumentException("Invalid token algorithm");
        }else {

            return parser.parseClaimsJws(token).getBody();
        }
    }

    private boolean isTokenExpired(String token) {
        return this.getAllClaimsFromToken(token).getExpiration().before(new Date());
    }

    public boolean isInvalid(String token) {
        return this.isTokenExpired(token);
    }

}
