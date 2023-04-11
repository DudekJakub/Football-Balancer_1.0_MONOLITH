package com.dudek.footballbalancer.aspect;

import com.dudek.footballbalancer.repository.RoomRepository;
import com.dudek.footballbalancer.validation.customAnnotation.RequiresRoomAdmin;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.util.List;
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

    //ZAMIAST SZUKAC PO NAZWIE POLA -> STWORZYC ADNOTACJE I SZUKAC POLA OKRASZONEGO TA ADNOTACJA

    @Around("@annotation(requiresRoomAdmin) && args(*,  @org.springframework.web.bind.annotation.RequestBody requestDto, ..)")
    public Object checkRoomAdmin(ProceedingJoinPoint joinPoint, RequiresRoomAdmin requiresRoomAdmin, Object requestDto) throws Throwable {
        long roomId = 0;
        long roomAdminId = 0;

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = methodSignature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < paramNames.length; i++) {
            if ("roomId".equals(paramNames[i]) && args[i] instanceof Long) {
                roomId = (Long) args[i];
                logger.debug("Room ID found as pathVariable or param - [{}]", roomId);
            } else if ("roomAdminId".equals(paramNames[i]) && args[i] instanceof Long) {
                roomAdminId = (Long) args[i];
                logger.debug("RoomAdmin ID found as pathVariable or param - [{}]", roomAdminId);
            }
        }

        if (roomId == 0 && requestDto != null) {
                List<String> fieldNames = Stream.of(requestDto.getClass().getDeclaredFields()).map(Field::getName).collect(Collectors.toList());
                if (!fieldNames.contains("roomId")) {
                    Field roomIdField = requestDto.getClass().getDeclaredField("roomId");
                    roomIdField.setAccessible(true);
                    roomId = (Long) roomIdField.get(requestDto);
                    logger.debug("Room ID found in requestBody - [{}]", roomId);
                }

        } else if (roomAdminId == 0 && requestDto != null) {
                List<String> fieldNames = Stream.of(requestDto.getClass().getDeclaredFields()).map(Field::getName).collect(Collectors.toList());
                if (fieldNames.contains("roomAdminId")) {
                    Field roomAdminIdField = requestDto.getClass().getDeclaredField("roomAdminId");
                    roomAdminIdField.setAccessible(true);
                    roomAdminId = (Long) roomAdminIdField.get(requestDto);
                    logger.debug("RoomAdmin ID found in requestBody - [{}]", roomAdminId);
                }
        }

        if (roomRepository.isAdminOfRoom(roomAdminId, roomId)) {
            return joinPoint.proceed();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }
}
