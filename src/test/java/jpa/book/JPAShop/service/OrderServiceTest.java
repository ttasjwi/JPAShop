package jpa.book.JPAShop.service;

import jpa.book.JPAShop.domain.Address;
import jpa.book.JPAShop.domain.Member;
import jpa.book.JPAShop.domain.Order;
import jpa.book.JPAShop.domain.OrderStatus;
import jpa.book.JPAShop.domain.item.Book;
import jpa.book.JPAShop.exception.NotEnoughStockException;
import jpa.book.JPAShop.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("상품 주문이 정상적으로 되는지 테스트")
    public void orderTest() {
        //given
        Member member = createMember();
        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;

        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);
        Order findOrder = orderRepository.findOne(orderId);

        //then
        assertThat(findOrder.getStatus()).as("상품 주문 상태는 ORDER").isSameAs(OrderStatus.ORDER);
        assertThat(findOrder.getOrderItems().size()).as("주문한 상품 종류 수가 정확해야 한다.").isEqualTo(1);
        assertThat(findOrder.getTotalPrice()).as("주문 가격은 가격*수량이다.").isEqualTo(10000 * orderCount);
        assertThat(book.getStockQuantity()).as("주문 수량만큼 재고가 줄어야 한다.").isEqualTo(10-orderCount);
    }



    @Test
    @DisplayName("상품주문 재고수량 초과")
    public void orderFailureTest() {
        //given
        Member member = createMember();
        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 11;

        //when * Then
        assertThatThrownBy(
                ()->orderService.order(member.getId(), book.getId(), orderCount))
                .isInstanceOf(NotEnoughStockException.class);
    }

    @Test
    @DisplayName("주문 취소 테스트")
    public void orderCancelTest() {
        //given
        Member member = createMember();
        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //when
        orderService.cancelOrder(orderId);

        //then
        Order findOrder = orderRepository.findOne(orderId);

        assertThat(findOrder.getStatus()).as("주문 취소 시 상태는 CANCEL이다.").isSameAs(OrderStatus.CANCEL);
        assertThat(book.getStockQuantity()).as("취소된 상품의 재고는 그만큼 증가해야한다.").isEqualTo(10);
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울특별시", "강남구", "123-123"));
        em.persist(member);
        return member;
    }

    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

}
