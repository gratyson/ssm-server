package com.gt.ssm.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(1)
public class SecurityGraphQlAspect {

    private static final Logger log = LoggerFactory.getLogger(SecurityGraphQlAspect.class);

    /**
     * All graphQLResolver methods can be called only by authenticated user.
     * @Unsecured annotated methods are excluded
     */
    @Before("allGraphQLResolverMethods() && isDefinedInApplication() && !isMethodAnnotatedAsUnsecured()")
    public void doSecurityCheck() {
        if (SecurityContextHolder.getContext() == null ||
                SecurityContextHolder.getContext().getAuthentication() == null ||
                !SecurityContextHolder.getContext().getAuthentication().isAuthenticated() ||
                AnonymousAuthenticationToken.class.isAssignableFrom(SecurityContextHolder.getContext().getAuthentication().getClass())) {
            throw new AccessDeniedException("User not authenticated");
        }

    }

    @Pointcut("isGraphQlQuery() || isGraphQlMutation()")
    private void allGraphQLResolverMethods() { }

    @Pointcut("@annotation(org.springframework.graphql.data.method.annotation.QueryMapping)")
    private void isGraphQlQuery() { }

    @Pointcut("@annotation(org.springframework.graphql.data.method.annotation.MutationMapping)")
    private void isGraphQlMutation() { }

    @Pointcut("within(com.gt.ssm..*)")
    private void isDefinedInApplication() { }

    @Pointcut("@annotation(com.gt.ssm.aspect.Unsecured)")
    private void isMethodAnnotatedAsUnsecured() { }
}
