package com.mgnt.ticketing.util.aop;

import com.mgnt.ticketing.common.error.exceptions.CustomAccessDeniedException;
import com.mgnt.ticketing.common.error.ErrorCode;
import com.mgnt.ticketing.entity.UserEntity;
import com.mgnt.ticketing.service.UserService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class CheckAccessAspect {

    private final UserService userService;

    @Before("@annotation(checkAccess) && args(id,..)")
    public void checkAccess(JoinPoint joinPoint, CheckAccess checkAccess, Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ADMIN"));

        if (!isAdmin) {
            switch (checkAccess.resourceType()) {
                case "USER":
                    UserEntity user = userService.getUserById(id).orElse(null);

                    if (user == null || !user.getEmail().equals(currentUserEmail))
                        throw new CustomAccessDeniedException(ErrorCode.ACCESS_DENIED);

                    break;
                // Add other cases for different resource types
                default:
                    throw new IllegalArgumentException("Invalid resource type: " + checkAccess.resourceType());
            }
        }
    }
}
