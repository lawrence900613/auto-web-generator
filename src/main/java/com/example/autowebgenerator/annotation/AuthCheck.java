package com.example.autowebgenerator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Permission check annotation.
 * Place on controller methods that require a specific user role.
 * Leave mustRole blank to require only that the user be logged in.
 *
 * Example:
 *   @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {

    /**
     * The minimum role required to call this method.
     * Defaults to "" (any logged-in user).
     */
    String mustRole() default "";
}
