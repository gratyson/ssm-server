package com.gt.ssm.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
class JwtCookieInterceptor implements WebGraphQlInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtCookieInterceptor.class);

    private final String jwtCookieName;
    private final long cookieExpirySec;

    public JwtCookieInterceptor(
            @Value("${server.jwt.cookieName}") String jwtCookieName,
            @Value("${server.jwt.cookieExpirySec}") long cookieExpirySec) {
        this.jwtCookieName = jwtCookieName;
        this.cookieExpirySec = cookieExpirySec;
    }

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        return chain.next(request).doOnNext((response) -> {
            if (response.getExecutionInput().getGraphQLContext().hasKey(jwtCookieName)) {
                String token = response.getExecutionInput().getGraphQLContext().get(jwtCookieName);

                ResponseCookie cookie = buildAuthCookie(token);
                response.getResponseHeaders().add(HttpHeaders.SET_COOKIE, cookie.toString());
            }
        });
    }

    private ResponseCookie buildAuthCookie(String token) {
        return ResponseCookie.from(jwtCookieName, token)
                .httpOnly(false)
                .secure(false)
                .path("/")
                .maxAge(cookieExpirySec)
                .build();
    }
}
