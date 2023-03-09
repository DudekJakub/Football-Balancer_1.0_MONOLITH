package com.dudek.footballbalancer.validation;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Predicate;

@Slf4j
public class DateValidator {

    public static boolean allDatesNotNullAndInOrder(final LocalDateTime firstDate, final LocalDateTime middleDate, final LocalDateTime lastDate) {
        Predicate<LocalDateTime> isNotNullPredicate = Objects::nonNull;
        Predicate<LocalDateTime> isOrderedPredicate = date -> date.equals(firstDate) || date.isAfter(firstDate);
        Predicate<LocalDateTime> isNotNullAndOrderedPredicate = isNotNullPredicate.and(isOrderedPredicate);

        boolean isNotNullAndOrdered = isNotNullAndOrderedPredicate.test(middleDate) && isNotNullAndOrderedPredicate.test(lastDate) && middleDate.isBefore(lastDate);

        if (isNotNullAndOrdered) {
            log.debug("Checking dates order... SUCCESS! First: " + firstDate + " | Middle: " + middleDate + " | Last: " + lastDate);
        } else {
            log.debug("Checking dates order... FAIL! First: " + firstDate + " | Middle: " + middleDate + " | Last: " + lastDate);
        }

        return isNotNullAndOrdered;
    }
}
