package com.damda.global.auth.filter;

import com.damda.global.auth.token.AuthToken;
import com.damda.global.auth.token.AuthTokenProvider;
import com.damda.global.auth.util.JwtHeaderUtil;
import com.damda.global.exception.BaseException;
import com.damda.global.exception.ErrorCode;
import com.damda.global.exception.ErrorRes;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthTokenProvider tokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final List<String> NO_CHECK_URLS = List.of(
            "/auth/signup",
            "/auth/login",
            "/auth/reissue",
            "/members/check"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return NO_CHECK_URLS.contains(path);  // 토큰 검사 제외
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            final String authorizationHeader = request.getHeader("Authorization");

            // JWT 토큰 존재하는지 확인
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String tokenStr = JwtHeaderUtil.getAccessToken(request);    // Bearer로 시작하는 값에서 Bearer를 제거한 accessToken 반환
                AuthToken token = tokenProvider.convertAuthToken(tokenStr); // String to AuthToken

                // token이 유효한지 확인
                if (token.validate()) {
                    Authentication authentication = tokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);   // token에 존재하는 authentication 정보 삽입

                    // subject (UUID) 추출 후 request에 저장
                    String memberId = token.getTokenClaims().getSubject();
                    request.setAttribute("memberId", UUID.fromString(memberId));
                }
            }

            filterChain.doFilter(request, response);

        } catch (BaseException e) {
            // JWT 관련 커스텀 예외 처리
            log.error("JWT 인증 오류: {}", e.getMessage());
            setErrorResponse(response, e);
        } catch (Exception e) {
            // 그 외 예외 처리
            log.error("JWT 필터 예외 발생", e);
            setErrorResponse(
                    response,
                    new BaseException(ErrorCode.INVALID_ACCESS_TOKEN)
            );
        }
    }

    /**
     * 필터에서 발생한 에러를 ErrorRes 형식으로 응답
     */
    private void setErrorResponse(HttpServletResponse response, BaseException exception) throws IOException {
        response.setStatus(exception.getErrorCode().getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorRes errorRes = ErrorRes.builder()
                .status(exception.getErrorCode().getStatus())
                .code(String.valueOf(exception.getErrorCode().getCode()))
                .message(exception.getErrorCode().getMessage())
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(errorRes));
    }
}
