package com.test.security.jwt;

import com.test.security.exceptions.JwtAuthenticationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public class JwtUtil {

  private static final String SECRET = "P2pToqjaemNSGhkpyR7CoajM2bGCYgBq";
  private static final String REFRESH_SECRET = "P2pToqjaemNSGhkpyR7CoajM2bGCYgBq";
  private static final String REFRESH_TOKEN_PROVIDER_ID = "Diploma";
  private static final long TOKEN_DURATION = 86400000; //1 day
  private static final long REFRESH_TOKEN_DURATION = 2592000000L; //30 days

  public static String generateToken(String username) {
    return Jwts.builder().setSubject(username)
        .setExpiration(new Date(System.currentTimeMillis() + TOKEN_DURATION))
        .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
        .compact();
  }

  public static String generateRefreshToken() {
    return Jwts.builder()
        .setSubject(REFRESH_TOKEN_PROVIDER_ID)
        .setIssuer(REFRESH_TOKEN_PROVIDER_ID)
        .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_DURATION))
        .signWith(Keys.hmacShaKeyFor(REFRESH_SECRET.getBytes()))
        .compact();
  }

  public static String extractUsername(String token) {
    String username = Jwts.parserBuilder()
        .setSigningKey(SECRET.getBytes())
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
    return username;
  }

  public static String getLoggedInUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth instanceof AnonymousAuthenticationToken) {
      return null;
    }
    return auth.getName();
  }

  public static void validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(SECRET.getBytes()).build().parseClaimsJws(token);
    } catch (RuntimeException e) {
      throw new JwtAuthenticationException(e.getMessage());
    }
  }

}
