package com.dudek.footballbalancer.validation;

import com.dudek.footballbalancer.model.Formation;
import com.dudek.footballbalancer.validation.customAnnotation.ValidFormation;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.stream.IntStream;

@Component
public class FormationValidator implements ConstraintValidator<ValidFormation, Formation> {

    @Override
    public boolean isValid(Formation input, ConstraintValidatorContext context) {
        String failedValidationMessage;
        String formationAsString = String.valueOf(input.getProvidedFormationSchema());
        IntStream eachSegmentOfFormationSize = formationAsString.chars();
        int[] eachSegmentOfFormationSizeArray = eachSegmentOfFormationSize.toArray();
        int calculatedQuantityOfFieldSections = eachSegmentOfFormationSizeArray.length;
        int calculatedPlayersQuantityPerTeam = eachSegmentOfFormationSize.sum();

        BiPredicate<Integer, Integer> accordancePredicate = Objects::equals;
        boolean testAccordanceOfPlayersQuantityPerTeam = accordancePredicate.test(calculatedPlayersQuantityPerTeam, input.getProvidedPlayersQuantityPerTeam());
        boolean testAccordanceOfFieldSectionsQuantity = accordancePredicate.test(calculatedQuantityOfFieldSections, input.getProvidedFieldSections().size());

        if (!testAccordanceOfPlayersQuantityPerTeam) {
            failedValidationMessage = "Formation schema doesn't match with players quantity per team!";
            createCustomMessage(context, failedValidationMessage);
            return false;
        }
        if (!testAccordanceOfFieldSectionsQuantity) {
            failedValidationMessage = "Provided number of field sections doesn't match with given formation schema!";
            createCustomMessage(context, failedValidationMessage);
            return false;
        }

        for (int i = 0; i < calculatedQuantityOfFieldSections; i++) {
            boolean testAccordanceOfPlayersQuantityPerSection = accordancePredicate.test(eachSegmentOfFormationSizeArray[i],
                                                                                    input.getProvidedFieldSections().get(i).getPlayersQuantity());

            if (!testAccordanceOfPlayersQuantityPerSection) {
                failedValidationMessage = "Field section at position [" + i + "] doesn't have required players quantity! " +
                                          "REQUIRED PLAYERS QUANTITY FOR GIVEN FIELD SECTION = " + eachSegmentOfFormationSizeArray[i];
                createCustomMessage(context, failedValidationMessage);
                return false;
            }
        }
        return true;
    }

    private void createCustomMessage(final ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context
                .buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}
