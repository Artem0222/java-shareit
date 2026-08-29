package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto create(Long userId, ItemRequestDto requestDto);

    List<ItemRequestWithItemsDto> findByRequestorId(Long userId);

    List<ItemRequestWithItemsDto> findAllOtherRequests(Long userId, Integer from, Integer size);

    ItemRequestWithItemsDto findById(Long requestId, Long userId);
}