package jpa.book.JPAShop.api;

import jpa.book.JPAShop.api.dto.SimpleOrderListResponse;
import jpa.book.JPAShop.domain.Order;
import jpa.book.JPAShop.domain.OrderSearch;
import jpa.book.JPAShop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *
 * xxToOne(ManyToOne, OneToOne)에서의 성능 최적화
 *  Order -> Member
 *  Order -> Delivery
 */

@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); // Lazy 강제 초기화
            order.getDelivery().getStatus(); // Lazy 강제 초기화
        }

        return all;
    }

    @GetMapping("/api/v2/simple-orders")
    public SimpleOrderListResponse ordersV2() {
        List<Order> orderEntities = orderRepository.findAllByString(new OrderSearch());
        return SimpleOrderListResponse.create(orderEntities);
    }

}
