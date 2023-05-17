package com.dudek.footballbalancer.aspect;

import com.dudek.footballbalancer.repository.RoomRepository;
import com.dudek.footballbalancer.validation.customAnnotation.RequiresRoomAdmin;
import com.dudek.footballbalancer.validation.customAnnotation.RoomAdminId;
import com.dudek.footballbalancer.validation.customAnnotation.RoomId;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Aspect
@Component
public class RoomAdminAspect {
    private final RoomRepository roomRepository;
    private final Logger logger = LoggerFactory.getLogger(RoomAdminAspect.class);

    public RoomAdminAspect(final RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Around("@annotation(requiresRoomAdmin) && args(*,  @org.springframework.web.bind.annotation.RequestBody requestDto, ..)")
    public Object checkRoomAdmin(ProceedingJoinPoint joinPoint, RequiresRoomAdmin requiresRoomAdmin, Object requestDto) throws Throwable {
        long roomId = 0;
        long roomAdminId = 0;

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Object[] args = joinPoint.getArgs();
        Annotation[][] paramAnnotations = methodSignature.getMethod().getParameterAnnotations();

        int argIndex = -1;

        for (int i = 0; i < paramAnnotations.length; i++) {
            for (Annotation annotation : paramAnnotations[i]) {
                if (annotation.annotationType() == RoomAdminId.class) {
                    argIndex = i;
                    roomAdminId = (Long) args[argIndex];
                    logger.debug("RoomAdmin ID found as pathVariable or param - [{}]", roomAdminId);
                } else if (annotation.annotationType() == RoomId.class) {
                    argIndex = i;
                    roomId = (Long) args[argIndex];
                    logger.debug("Room ID found as pathVariable or param - [{}]", roomId);
                }
            }
        }

        if (roomId == 0 && requestDto != null) {
            List<Field> targetFields = Stream.of(requestDto.getClass().getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(RoomId.class))
                    .collect(Collectors.toList());
            AtomicLong id = new AtomicLong();
            targetFields.stream().findFirst().ifPresent(field -> {
                field.setAccessible(true);
                try {
                    id.set((Long) field.get(requestDto));
                    logger.debug("Room ID found in requestBody - [{}]", id);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
            roomId = id.get();
        }

        if (roomAdminId == 0 && requestDto != null) {
            List<Field> targetFields = Stream.of(requestDto.getClass().getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(RoomAdminId.class))
                    .collect(Collectors.toList());
            AtomicLong id = new AtomicLong();
            targetFields.stream().findFirst().ifPresent(field -> {
                field.setAccessible(true);
                try {
                    id.set((Long) field.get(requestDto));
                    logger.debug("RoomAdmin ID found in requestBody - [{}]", id);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
            roomAdminId = id.get();
        }

        if (roomRepository.isAdminOfRoom(roomAdminId, roomId)) {
            return joinPoint.proceed();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }
}
