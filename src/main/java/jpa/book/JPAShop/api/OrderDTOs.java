package jpa.book.JPAShop.api;

import jpa.book.JPAShop.domain.Address;
import jpa.book.JPAShop.domain.Order;
import jpa.book.JPAShop.domain.OrderItem;
import jpa.book.JPAShop.domain.OrderStatus;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
public class OrderDTOs {

    private final List<OrderDTO> orders;

    public static OrderDTOs create(List<Order> orderEntities) {
        List<OrderDTO> orders = orderEntities.stream()
                .map(OrderDTO::new)
                .collect(Collectors.toList());

        return new OrderDTOs(orders);
    }

    @Data
    static class OrderDTO {

        private Long orderId;
        private String customerName;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDTO> orderItems;

        public OrderDTO(Order orderEntity) {
            orderId = orderEntity.getId();
            customerName = orderEntity.getMember().getName();
            orderDate = orderEntity.getOrderDate();
            orderStatus = orderEntity.getStatus();
            address = orderEntity.getDelivery().getAddress();
            orderItems = initOrderItems(orderEntity.getOrderItems());
        }

        private List<OrderItemDTO> initOrderItems(List<OrderItem> orderItems) {
            return orderItems.stream()
                    .map(OrderItemDTO::new)
                    .collect(Collectors.toList());
        }

        @Data
        static class OrderItemDTO {

            private String itemName;
            private int orderPrice; // 주문 가격
            private int count; // 수량1

            public OrderItemDTO(OrderItem orderItem) {
                itemName = orderItem.getItem().getName();
                orderPrice = orderItem.getOrderPrice();
                count = orderItem.getCount();
            }
        }
    }
}
