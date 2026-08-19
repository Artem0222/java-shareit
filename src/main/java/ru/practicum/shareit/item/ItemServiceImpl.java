package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final ItemMapper itemMapper;

    @Override
    public List<ItemWithBookingsDto> findAllByOwnerId(Long ownerId) {
        if (!userRepository.existsById(ownerId)) {
            throw new NotFoundException("Пользователь с ид " + ownerId + " не найден");
        }
        List<Item> items = itemRepository.findByOwnerId(ownerId);
        return items.stream()
                .map(item -> enrichItemWithBookingsAndComments(item, ownerId))
                .collect(Collectors.toList());
    }

    @Override
    public ItemWithBookingsDto findById(Long id, Long userId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь с ид " + id + " не найдена"));
        return enrichItemWithBookingsAndComments(item, userId);
    }

    @Override
    @Transactional
    public ItemDto create(Long ownerId, ItemDto itemDto) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException
                        ("Пользователь с ид " + ownerId + " не найден"));

        Item item = itemMapper.toItem(itemDto);
        item.setOwner(owner);

        Item savedItem = itemRepository.save(item);
        return itemMapper.toItemDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto update(Long itemId, Long ownerId, ItemDto itemDto) {
        if (!userRepository.existsById(ownerId)) {
            throw new NotFoundException("Пользователь с ид " + ownerId + " не найден");
        }

        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ид " + itemId + " не найдена"));

        if (!existingItem.getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("Пользователь с ид " + ownerId + " не является владельцем вещи");
        }

        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(existingItem);
        return itemMapper.toItemDto(updatedItem);
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.search(text).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long itemId, Long userId, CommentDto commentDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException
                        ("Вещь с ид " + itemId + " не найдена"));

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException
                        ("Пользователь с ид " + userId + " не найден"));

        boolean hasBooked = bookingRepository.existsByItemIdAndBookerIdAndEndBeforeAndStatus(itemId, userId, LocalDateTime.now()
        );

        if (!hasBooked) {
            throw new BadRequestException
                    ("Пользователь не брал эту вещь в аренду или срок аренды не завершен");

        }
        Comment comment = commentMapper.toComment(commentDto);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toCommentDto(savedComment);
    }

    private ItemWithBookingsDto enrichItemWithBookingsAndComments(Item item, Long userId) {
        ItemWithBookingsDto dto = itemMapper.toItemWithBookingsDto(item);

        List<Comment> comments = commentRepository.findByItemId(item.getId());
        dto.setComments(commentMapper.toCommentDtoList(comments));

        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();

            List<Booking> bookings = bookingRepository.
                    findApprovedByItemIdOrderByStartDesc(item.getId());

            bookings.stream()
                    .filter(b -> b.getEnd().isBefore(now))
                    .findFirst()
                    .ifPresent(b -> dto.setLastBooking
                            (new BookingItemDto(b.getId(), b.getBooker().getId(), b.getStart(), b.getEnd()
                            )));

            bookings.stream()
                    .filter(b -> b.getStart().isAfter(now))
                    .reduce((first, second) -> second) // последнее в списке будущих
                    .ifPresent(b -> dto.setNextBooking(new BookingItemDto(
                            b.getId(), b.getBooker().getId(), b.getStart(), b.getEnd()
                    )));
        }
        return dto;
    }
}
