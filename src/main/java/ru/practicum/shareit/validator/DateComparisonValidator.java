package ru.practicum.shareit.validator;

import ru.practicum.shareit.booking.dto.BookingCreateDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class DateComparisonValidator implements ConstraintValidator<DateComparisonConstraint, BookingCreateDto> {

    @Override
    public void initialize(DateComparisonConstraint constraint) {
    }

    @Override
    public boolean isValid(BookingCreateDto model, ConstraintValidatorContext context) {
        LocalDateTime startDate = model.getStart();
        LocalDateTime endDate = model.getEnd();
        if (startDate == null || endDate == null) {
            return false;
        }
        // Проверяем, чтобы endDate был после startDate
        return endDate.isAfter(startDate);
    }
}

