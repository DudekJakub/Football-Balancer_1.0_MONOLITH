package com.dudek.footballbalancer.service.util;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Map;
import java.util.Objects;

public class RequestContextHolderUtil {

    public static final String TARGET_ROOM = "targetRoom";
    public static final String TARGET_PLAYER = "targetPlayer";
    public static final String TARGET_USER = "targetUser";
    public static final String PLAYER_FIRSTNAME_BEFORE_UPDATE = "playerFirstNameBeforeUpdate";
    public static final String PLAYER_LASTNAME_BEFORE_UPDATE = "playerLastNameBeforeUpdate";
    public static final String NEW_MEMBER_REQUEST = "newMemberRequest";

    public static void setContextAttribute(String name, Object targetObject) {
        Objects.requireNonNull(RequestContextHolder.getRequestAttributes())
                .setAttribute(name, targetObject, RequestAttributes.SCOPE_REQUEST);
    }

    public static void setContextAttributes(Map<String, Object> attributeMap) {
        attributeMap.forEach((name, targetObject) -> {
            Objects.requireNonNull(RequestContextHolder.getRequestAttributes())
                    .setAttribute(name, targetObject, RequestAttributes.SCOPE_REQUEST);
        });
    }

    public static Object getContextAttribute(String name) {
        return Objects.requireNonNull(RequestContextHolder.getRequestAttributes())
                .getAttribute(name, RequestAttributes.SCOPE_REQUEST);
    }
}
