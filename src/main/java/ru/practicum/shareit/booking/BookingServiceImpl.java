package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingResponseDto create(Long userId, BookingRequestDto requestDto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ид " + userId + " не найлен"));

        Item item = itemRepository.findById(requestDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с ид " + requestDto.getItemId() + " не найдена"));

        if (item.getOwner().getId().equals(userId)) {
            throw new BadRequestException("Нельзя бронировать свою вещь");
        }

        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new BadRequestException("Вещь недопустпна для бронирования");
        }

        if (requestDto.getStart().isAfter(requestDto.getEnd()) ||
                requestDto.getStart().equals(requestDto.getEnd())) {
            throw new BadRequestException("Дата окончания должна быть позже даты начала");
        }

        if (requestDto.getStart().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Дата начала не может быть в прошлом");
        }

        Booking booking = bookingMapper.toBooking(requestDto);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toBookingResponseDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto approve(Long bookingId, Long ownerId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с ид " + bookingId + " не найдено"));

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new NotFoundException("Пользователь не являвется владельцем вещи");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new BadRequestException("Бронирование уже обработано");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updateBooking = bookingRepository.save(booking);
        return bookingMapper.toBookingResponseDto(updateBooking);
    }

    @Override
    public BookingResponseDto findById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с ид " + bookingId + "не найдено"));

        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("У пользователя нет доступа к этому бронированию");
        }
        return bookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> findByBookerId(Long bookerId, BookingState state) {
        if (!userRepository.existsById(bookerId)) {
            throw new NotFoundException("Пользователь с ид " + bookerId + " не найден");
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state) {
            case ALL:
                bookings = bookingRepository.findByBookerId(bookerId, sort);
                break;
            case CURRENT:
                bookings = bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(
                        bookerId, now, now, sort);
                break;
            case PAST:
                bookings =
                        bookingRepository.findByBookerIdAndEndBefore(bookerId, now, sort);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartAfter(bookerId, now, sort);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatus(bookerId, BookingStatus.WAITING, sort);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatus(bookerId, BookingStatus.REJECTED, sort);
                break;
            default:
                throw new IllegalArgumentException("Некорректный статус: " + state);
        }

        return bookings.stream()
                .map(bookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> findByOwnerId(Long ownerId, BookingState state) {
        if (!userRepository.existsById(ownerId)) {
            throw new NotFoundException("Пользователь с ид " + ownerId + " не найден");
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state) {
            case ALL:
                bookings = bookingRepository.findAllByOwnerId(ownerId, sort);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllByOwnerIdAndStartBeforeAndEndAfter(
                        ownerId, now, sort);
                break;
            case PAST:
                bookings = bookingRepository.findAllByOwnerIdAndEndBefore(ownerId, now, sort);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByOwnerIdAndStartAfter(ownerId, now, sort);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByOwnerIdAndStatus(ownerId, BookingStatus.WAITING, sort);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByOwnerIdAndStatus(ownerId, BookingStatus.REJECTED, sort);
                break;
            default:
                throw new IllegalArgumentException("Некорректный статус: " + state);
        }

        return bookings.stream()
                .map(bookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }
}





