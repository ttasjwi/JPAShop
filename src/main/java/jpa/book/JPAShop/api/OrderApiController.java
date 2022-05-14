package jpa.book.JPAShop.api;


import jpa.book.JPAShop.api.dto.*;
import jpa.book.JPAShop.domain.Order;
import jpa.book.JPAShop.domain.OrderItem;
import jpa.book.JPAShop.domain.OrderSearch;
import jpa.book.JPAShop.repository.OrderRepository;
import jpa.book.JPAShop.repository.order.query.OrderQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

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

    @GetMapping("/api/v4/orders")
    public OrderQueryDTOs ordersV4() {
        return orderQueryRepository.findOrderQueryDTOs();
    }

    @GetMapping("/api/v5/orders")
    public OrderQueryDTOs ordersV5() {
        return orderQueryRepository.findOrderQueryDTOs_Optimization();
    }

    @GetMapping("/api/v6/orders")
    public OrderQueryDTOs ordersV6() {
        List<OrderFlatDTO> flats = orderQueryRepository.findOrderQueryDTOs_flat();

        List<OrderQueryDTO> orders = flats.stream()
                .collect(
                        // 그룹핑 : 각각의 OrderQueryDTO를 기준으로 그룹핑
                        groupingBy(flat -> new OrderQueryDTO(flat.getOrderId(), flat.getCustomerName(), flat.getOrderDate(), flat.getOrderStatus(), flat.getAddress()),

                        // 수집대상 : OrderItemQueryDTO들을 List로 수집
                        mapping(flat -> new OrderItemQueryDTO(flat.getOrderId(), flat.getItemName(), flat.getOrderPrice(), flat.getCount()), toList())
                )).entrySet()
                .stream()
                .map(
                        // OrderQueryDTO에 List<OrderItemQueryDTO>를 추가한 OrderQueryDTO 생성
                        entry -> new OrderQueryDTO(entry.getKey().getOrderId(), entry.getKey().getCustomerName(), entry.getKey().getOrderDate(), entry.getKey().getOrderStatus(), entry.getKey().getAddress(), entry.getValue()))
                .collect(toList());
        return new OrderQueryDTOs(orders);
    }
}
