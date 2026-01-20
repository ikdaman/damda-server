package com.damda.global.auth.token;

import com.damda.global.auth.enumerate.RoleType;
import com.damda.global.auth.service.MemberDetailsService;
import com.damda.global.exception.BaseException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import static com.damda.global.exception.ErrorCode.FAILED_GENERATE_APP_TOKEN;

@Slf4j
@Component
public class AuthTokenProvider {
    @Value("${auth.access-token-validity}")
    private String accessExpiry; // AccessToken 만료일

    @Value("${auth.refresh-token-validity}")
    private String refreshExpiry; // RefreshToken 만료일

    private final Key key;
    private static final String AUTHORITIES_KEY = "role"; // getAuthentication에서 사용자 권한 체크 위해

    private final MemberDetailsService memberDetailsService;

    //생성자
    public AuthTokenProvider(@Value("${auth.tokenSecret}") String secretKey, MemberDetailsService memberDetailsService) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.memberDetailsService = memberDetailsService;
    }

    // 추후 roleType 추가 시 interface 역할 하기 위해 생성
    // role: USER, ADMIN ..
    public AuthToken createToken(String id, RoleType roleType, String expiry, String tokenType) {
        Date expiryDate = getExpiryDate(expiry);
        return new AuthToken(id, roleType, expiryDate, key, tokenType);
    }

    // USER에 대한 AccessToken(여기선 appToken) 생성
    public AuthToken createUserAppToken(String id) {
        return createToken(id, RoleType.USER, accessExpiry, "access");
    }

    // RefreshToken 생성
    public AuthToken createRefreshToken(String id) {
        return createToken(id, RoleType.USER, refreshExpiry, "refresh");
    }

    // String to AuthToken
    public AuthToken convertAuthToken(String token) {
        return new AuthToken(token, key);
    }

    // String to Date
    public static Date getExpiryDate(String expiry) {
        return new Date(System.currentTimeMillis() + Long.parseLong(expiry));
    }

    public Authentication getAuthentication(AuthToken authToken) {

        if (authToken.validate()) {

            Claims claims = authToken.getTokenClaims();

            Collection<? extends GrantedAuthority> authorities =
                    Arrays.stream(new String[]{claims.get(AUTHORITIES_KEY).toString()})
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

            UserDetails member = memberDetailsService.loadUserByUsername(claims.getSubject());
            return new UsernamePasswordAuthenticationToken(member, authToken, authorities);
        } else {
            throw new BaseException(FAILED_GENERATE_APP_TOKEN);
        }
    }
}
