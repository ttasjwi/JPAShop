package jpa.book.JPAShop.repository.order.query;

import jpa.book.JPAShop.api.dto.OrderItemQueryDTO;
import jpa.book.JPAShop.api.dto.OrderQueryDTO;
import jpa.book.JPAShop.api.dto.OrderQueryDTOs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    public OrderQueryDTOs findOrderQueryDTOs() {
        List<OrderQueryDTO> findOrders = findOrders();
        findOrders.forEach(o-> o.setOrderItems(findOrderItems(o.getOrderId())));
        return new OrderQueryDTOs(findOrders);
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

}
