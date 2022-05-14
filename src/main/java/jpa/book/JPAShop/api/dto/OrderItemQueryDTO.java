package jpa.book.JPAShop.api.dto;

import lombok.Data;

@Data
public class OrderItemQueryDTO {

    private Long orderId;
    private String itemName;
    private int orderPrice; // 주문 가격
    private int count;

    public OrderItemQueryDTO(Long orderId, String itemName, int orderPrice, int count) {
        this.orderId = orderId;
        this.itemName = itemName;
        this.orderPrice = orderPrice;
        this.count = count;
    }
}
