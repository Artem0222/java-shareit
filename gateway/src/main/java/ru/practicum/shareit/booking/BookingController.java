package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Valid @RequestBody BookingRequestDto requestDto) {
        return bookingClient.create(userId, requestDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approve(
            @PathVariable Long bookingId,
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam Boolean approved) {
        return bookingClient.approve(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> findById(
            @PathVariable Long bookingId,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingClient.findById(bookingId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> findByBooker(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") BookingState state) {
        return bookingClient.findByBooker(userId, state);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> findByOwner(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") BookingState state) {
        return bookingClient.findByOwner(userId, state);
    }
}