package jpa.book.JPAShop.api.dto;

import jpa.book.JPAShop.domain.Address;
import jpa.book.JPAShop.domain.Order;
import jpa.book.JPAShop.domain.OrderStatus;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Data
public class SimpleOrderListResponse {

    private final List<SimpleOrderListElement> orders;

    public static SimpleOrderListResponse create(List<Order> orderEntities) {
        List<SimpleOrderListElement> orders = orderEntities.stream()
                .map(SimpleOrderListElement::new)
                .collect(Collectors.toList());
        return new SimpleOrderListResponse(orders);
    }

    @Data
    static class SimpleOrderListElement {

        private Long orderId;
        private String customerName;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderListElement(Order orderEntity) {
            orderId = orderEntity.getId();
            customerName = orderEntity.getMember().getName();
            orderDate = orderEntity.getOrderDate();
            orderStatus = orderEntity.getStatus();
            address = orderEntity.getDelivery().getAddress();
        }
    }
}
