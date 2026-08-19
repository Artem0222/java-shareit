package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;

@Data
public class BookingDto {
    private Long id;

    @NotNull(message = "Дата начала не модкт быть пустой")
    @Future(message = "Дата начала не доллдна быть в будущем")
    private LocalDateTime start;

    @NotNull(message = "Дата окончачиня не может быть пустой")
    @Future(message = "Дата окончаниая не может быть в будущем")
    private LocalDateTime end;

    private Long itemId;
    private Long bookerId;
    private BookingStatus status;
}
