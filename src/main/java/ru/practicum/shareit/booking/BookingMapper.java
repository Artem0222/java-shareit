package ru.practicum.shareit.booking;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

@Component
public class BookingMapper {

    public Booking toBooking(BookingRequestDto dto) {
        if (dto == null) return null;

        Booking booking = new Booking();
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        return booking;
    }

    public BookingResponseDto toBookingResponseDto(Booking booking) {
        if (booking == null) return null;

        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setStatus(booking.getStatus());

        if (booking.getItem() != null) {
            dto.setItem(new BookingResponseDto.ItemInfo(
                    booking.getItem().getId(),
                    booking.getItem().getName()
            ));
        }

        if (booking.getBooker() != null) {
            dto.setBooker(new BookingResponseDto.UserInfo(
                    booking.getBooker().getId()
            ));
        }

        return dto;
    }
}