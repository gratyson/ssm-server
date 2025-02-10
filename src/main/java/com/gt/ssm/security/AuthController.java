package com.gt.ssm.security;

import com.gt.ssm.aspect.Unsecured;
import com.gt.ssm.exception.InvalidUserDetailsException;
import com.gt.ssm.security.model.QlAuthResponse;
import com.gt.ssm.security.model.QlUserInput;
import graphql.GraphQLContext;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

@Controller
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);


    private final AuthenticationManager authenticationManager;
    private final LocalUserDetailsManager localUserDetailsManager;
    private final JwtService jwtService;
    private final String jwtCookieName;
    private final long cookieExpirySec;

    public AuthController(AuthenticationManager authenticationManager,
                          LocalUserDetailsManager localUserDetailsManager,
                          JwtService jwtService,
                          @Value("${server.jwt.cookieName}") String jwtCookieName,
                          @Value("${server.jwt.cookieExpirySec}") long cookieExpirySec) {
        this.authenticationManager = authenticationManager;
        this.localUserDetailsManager = localUserDetailsManager;
        this.jwtService = jwtService;
        this.jwtCookieName = jwtCookieName;
        this.cookieExpirySec = cookieExpirySec;
    }

    @Unsecured
    @QueryMapping
    public String loggedInUsername(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null && userDetails.getUsername() != null) {
            return userDetails.getUsername();
        }

        return "";
    }


    @Unsecured
    @MutationMapping
    public QlAuthResponse login(@Argument QlUserInput user, GraphQLContext context) {
        String accessToken = AuthenticateAndBuildAccessToken(user.username(), user.password());
        if (accessToken == null) {
            return new QlAuthResponse(false, "Login unsuccessful");
        }

        context.put(jwtCookieName, accessToken);
        return new QlAuthResponse(true, "");
    }

    @Unsecured
    @MutationMapping
    public QlAuthResponse register(@Argument QlUserInput user, GraphQLContext context) {
        try {
            localUserDetailsManager.createUser(user.username(), user.password());
        } catch (InvalidUserDetailsException ex) {
            return new QlAuthResponse(false, ex.getMessage());
        }

        String accessToken = AuthenticateAndBuildAccessToken(user.username(), user.password());
        if (accessToken == null) {
            return new QlAuthResponse(false, "Failed to save user");
        }

        context.put(jwtCookieName, accessToken);
        return new QlAuthResponse(true, "");
    }

    @MutationMapping
    public QlAuthResponse logout(GraphQLContext context) {
        ResponseCookie cookie = buildAuthCookie(null, 0);
        context.put(jwtCookieName, "");

        return new QlAuthResponse(true, "");
    }

    private String AuthenticateAndBuildAccessToken(String username, String password) {
        Authentication authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(username, password);
        Authentication authenticationResponse;

        try {
            authenticationResponse = this.authenticationManager.authenticate(authenticationRequest);
        } catch (BadCredentialsException ex) {
            return null;
        }

        if (authenticationResponse.isAuthenticated()) {
            return jwtService.generateToken(username);
        }
        return null;
    }

    private ResponseCookie buildAuthCookie(String token, long expirySec) {
        return ResponseCookie.from(jwtCookieName, token)
                .httpOnly(false)
                .secure(false)
                .path("/")
                .maxAge(expirySec)
                .build();
    }

    private record LoginRequest(String username, String password) { }
    private record LoginResponse(boolean success, String errMsg) { }

    private record RegisterRequest(String username, String password, String reenterPassword) { }

    private record RegisterResponse(boolean success, String errMsg) { }
}
