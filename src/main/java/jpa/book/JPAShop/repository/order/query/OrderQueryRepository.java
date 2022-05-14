package jpa.book.JPAShop.repository.order.query;

import jpa.book.JPAShop.api.dto.OrderFlatDTO;
import jpa.book.JPAShop.api.dto.OrderItemQueryDTO;
import jpa.book.JPAShop.api.dto.OrderQueryDTO;
import jpa.book.JPAShop.api.dto.OrderQueryDTOs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    public OrderQueryDTOs findOrderQueryDTOs() {
        List<OrderQueryDTO> findOrders = findOrders();
        findOrders.forEach(o-> o.setOrderItems(findOrderItems(o.getOrderId())));
        return new OrderQueryDTOs(findOrders);
    }

    public OrderQueryDTOs findOrderQueryDTOs_Optimization() {
        List<OrderQueryDTO> findOrders = findOrders();

        List<Long> orderIds = toOrderIds(findOrders);
        Map<Long, List<OrderItemQueryDTO>> orderItemMap = findOrderItemMap(orderIds);

        findOrders.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));
        return new OrderQueryDTOs(findOrders);
    }

    private List<Long> toOrderIds(List<OrderQueryDTO> orderDTOs) {
        return orderDTOs.stream()
                .map(OrderQueryDTO::getOrderId)
                .collect(toList());
    }

    private Map<Long, List<OrderItemQueryDTO>> findOrderItemMap(List<Long> orderIds) {
        return em.createQuery(
                        "SELECT " +
                                "new jpa.book.JPAShop.api.dto.OrderItemQueryDTO(oi.order.id, i.name, oi.orderPrice, oi.count) "+
                                "FROM OrderItem as oi " +
                                "JOIN oi.item as i " +
                                "WHERE oi.order.id in :orderIds", OrderItemQueryDTO.class)
                .setParameter("orderIds", orderIds)
                .getResultStream()
                .collect(groupingBy(OrderItemQueryDTO::getOrderId));
    }

    private List<OrderQueryDTO> findOrders() {
        return em.createQuery(
                        "SELECT " +
                                "new jpa.book.JPAShop.api.dto.OrderQueryDTO(o.id, m.name, o.orderDate, o.status, d.address) " +
                                "FROM Order as o " +
                                "join o.member as m " +
                                "join o.delivery as d", OrderQueryDTO.class)
                .getResultList();
    }

    private List<OrderItemQueryDTO> findOrderItems(Long orderId) {
        return em.createQuery(
                        "SELECT " +
                                "new jpa.book.JPAShop.api.dto.OrderItemQueryDTO(oi.order.id, i.name, oi.orderPrice, oi.count) "+
                                "FROM OrderItem as oi " +
                                "JOIN oi.item as i " +
                                "WHERE oi.order.id = :orderId", OrderItemQueryDTO.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    public List<OrderFlatDTO> findOrderQueryDTOs_flat() {
        return em.createQuery(
                "SELECT " +
                        "new jpa.book.JPAShop.api.dto.OrderFlatDTO" +
                        "(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count) " +
                        "FROM Order as o " +
                        "JOIN o.member as m " +
                        "JOIN o.delivery as d " +
                        "JOIN o.orderItems as oi " +
                        "JOIN oi.item as i", OrderFlatDTO.class)
                .getResultList();
    }
}
