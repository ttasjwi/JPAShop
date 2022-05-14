package jpa.book.JPAShop.api.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class OrderQueryDTOs {

    private final List<OrderQueryDTO> orders;
}
