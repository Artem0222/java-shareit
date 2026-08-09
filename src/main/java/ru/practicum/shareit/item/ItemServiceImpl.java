package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;
    private final ItemMapper itemMapper;

    @Override
    public List<ItemDto> findAllByOwnerId(Long ownerId) {
        if (!userStorage.existsById(ownerId)) {
            throw new NotFoundException("Пользователь с ид " + ownerId + " не найден");
        }
        return itemStorage.findAllByOwnerId(ownerId).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto findById(Long id) {
        Item item = itemStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь с ид " + id + " не найдена"));
        return itemMapper.toItemDto(item);
    }

    @Override
    public ItemDto create(Long ownerId, ItemDto itemDto) {
        User owner = userStorage.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ид " + ownerId + " не найден"));

        Item item = itemMapper.toItem(itemDto);
        item.setOwner(owner);

        Item savedItem = itemStorage.save(item);
        return itemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto update(Long itemId, Long ownerId, ItemDto itemDto) {
        if (!userStorage.existsById(ownerId)) {
            throw new NotFoundException("Пользователь с ид " + ownerId + " не найден");
        }

        Item existingItem = itemStorage.findById(itemId)
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

        Item updatedItem = itemStorage.update(existingItem);
        return itemMapper.toItemDto(updatedItem);
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemStorage.search(text).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}