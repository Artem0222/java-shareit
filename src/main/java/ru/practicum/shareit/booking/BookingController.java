package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingResponseDto create(
            @RequestHeader("X-Share-User-Id") Long userId,
            @Valid @RequestBody BookingRequestDto requestDto) {
        return bookingService.create(userId, requestDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approve(
            @PathVariable Long bookingId,
            @RequestHeader("X-Share-User-Id") Long userId,
            @RequestParam Boolean approved) {
        return bookingService.approve(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto findById(
            @PathVariable Long bookingId,
            @RequestHeader("X-Share-User-Id") Long userId) {
        return bookingService.findById(bookingId, userId);
    }

    @GetMapping
    public List<BookingResponseDto> findByBooker(
            @RequestHeader("X-Share-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") BookingState state) {
        return bookingService.findByBookerId(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> findByOwner(
            @RequestHeader("X-Share-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") BookingState state) {
        return bookingService.findByOwnerId(userId, state);
    }
}
