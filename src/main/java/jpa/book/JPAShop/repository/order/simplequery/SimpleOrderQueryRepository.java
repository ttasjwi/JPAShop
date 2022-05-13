package jpa.book.JPAShop.repository.order.simplequery;

import jpa.book.JPAShop.api.dto.SimpleOrderQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SimpleOrderQueryRepository {

    private final EntityManager em;
    public List<SimpleOrderQueryDto> findOrderDtos() {
        return em.createQuery(
                        "SELECT " +
                                "new jpa.book.JPAShop.api.dto.SimpleOrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address) " +
                                "from Order as o join o.member as m join o.delivery d", SimpleOrderQueryDto.class)
                .getResultList();
    }
}
