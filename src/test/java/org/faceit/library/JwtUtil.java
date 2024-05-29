package org.faceit.library;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtil {
    private static final String signingKey = "413F4428472B4B6250655368566D5970337336763979244226452948404D6351";
    private static final long expirationTimeInMilliSeconds = 3_600_000; // 1 hour

    public static String createToken(String username) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationTimeInMilliSeconds);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private static Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(signingKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}