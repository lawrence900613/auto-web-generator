package com.example.autowebgenerator.aop;

import com.example.autowebgenerator.annotation.AuthCheck;
import com.example.autowebgenerator.exception.ErrorCode;
import com.example.autowebgenerator.exception.ExceptionUtils;
import com.example.autowebgenerator.model.entity.User;
import com.example.autowebgenerator.model.enums.UserRoleEnum;
import com.example.autowebgenerator.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * AOP interceptor that enforces @AuthCheck role requirements.
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();

        // Retrieve the current HTTP request from the context
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        // Get the currently logged-in user (throws UNAUTHORIZED if not logged in)
        User loginUser = userService.getLoginUser(request);

        // If no specific role is required, allow any logged-in user
        if (mustRole == null || mustRole.isBlank()) {
            return joinPoint.proceed();
        }

        // Validate that the user's role satisfies the required role
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        ExceptionUtils.throwIf(mustRoleEnum == null, ErrorCode.FORBIDDEN, "Access denied");

        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        ExceptionUtils.throwIf(userRoleEnum == null, ErrorCode.FORBIDDEN, "Access denied");

        // Only admin can access admin-only endpoints
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum)) {
            ExceptionUtils.throwIf(!UserRoleEnum.ADMIN.equals(userRoleEnum),
                    ErrorCode.FORBIDDEN, "Admin access required");
        }

        return joinPoint.proceed();
    }
}
