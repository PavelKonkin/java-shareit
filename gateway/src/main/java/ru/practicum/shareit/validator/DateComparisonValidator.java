package ru.practicum.shareit.validator;

import ru.practicum.shareit.booking.dto.BookItemRequestDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class DateComparisonValidator implements ConstraintValidator<DateComparisonConstraint, BookItemRequestDto> {

    @Override
    public void initialize(DateComparisonConstraint constraint) {
    }

    @Override
    public boolean isValid(BookItemRequestDto model, ConstraintValidatorContext context) {
        LocalDateTime startDate = model.getStart();
        LocalDateTime endDate = model.getEnd();
        if (startDate == null || endDate == null) {
            return false;
        }

        return endDate.isAfter(startDate);
    }
}

