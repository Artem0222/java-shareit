package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {

    private final ItemRequestService requestService;

    @PostMapping
    public ItemRequestDto create(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Valid @RequestBody ItemRequestDto requestDto) {
        return requestService.create(userId, requestDto);
    }

    @GetMapping
    public List<ItemRequestWithItemsDto> findByRequestor(
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        return requestService.findByRequestorId(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestWithItemsDto> findAllOtherRequests(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "20") Integer size) {
        return requestService.findAllOtherRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestWithItemsDto findById(
            @PathVariable Long requestId,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        return requestService.findById(requestId, userId);
    }
}


