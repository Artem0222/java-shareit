package ru.practicum.shareit.request;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItemRequestMapper {

    public ItemRequestDto toDto(ItemRequest request) {
        if (request == null) return null;

        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setCreated(request.getCreated());
        return dto;
    }

    public ItemRequest toEntity(ItemRequestDto dto) {
        if (dto == null) return null;

        ItemRequest request = new ItemRequest();
        request.setDescription(dto.getDescription());
        return request;
    }

    public ItemRequestWithItemsDto toDtoWithItems(ItemRequest request) {
        if (request == null) return null;

        ItemRequestWithItemsDto dto = new ItemRequestWithItemsDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setCreated(request.getCreated());

        if (request.getItems() != null) {
            List<ItemRequestWithItemsDto.ItemResponseDto> itemDtos = request.getItems().stream()
                    .map(item -> new ItemRequestWithItemsDto.ItemResponseDto(
                            item.getId(),
                            item.getName(),
                            item.getOwner().getId()
                    ))
                    .collect(Collectors.toList());
            dto.setItems(itemDtos);
        }

        return dto;
    }
}