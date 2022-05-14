package jpa.book.JPAShop.api;


import jpa.book.JPAShop.api.dto.OrderDTOs;
import jpa.book.JPAShop.domain.Order;
import jpa.book.JPAShop.domain.OrderItem;
import jpa.book.JPAShop.domain.OrderSearch;
import jpa.book.JPAShop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getStatus();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
        }
        return all;
    }

    @GetMapping("/api/v2/orders")
    public OrderDTOs ordersV2() {
        List<Order> orderEntities = orderRepository.findAllByString(new OrderSearch());
        return OrderDTOs.create(orderEntities);
    }

    @GetMapping("/api/v3/orders")
    public OrderDTOs ordersV3() {
        List<Order> orderEntities = orderRepository.findAllWithItem();
        return OrderDTOs.create(orderEntities);
    }

    @GetMapping("/api/v3.1/orders")
    public OrderDTOs ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {

        List<Order> orderEntities = orderRepository.findAllWithMemberDelivery(offset, limit);
        return OrderDTOs.create(orderEntities);

    }
}
