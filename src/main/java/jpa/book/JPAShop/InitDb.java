package jpa.book.JPAShop;

import jpa.book.JPAShop.domain.*;
import jpa.book.JPAShop.domain.item.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

/**
 * 조회용 샘플 데이터
 * 총 주문 2개
 * 
 * - 땃쥐
 *    - 땃쥐의 스프링
 *    - 자바 ORM 표준 JPA 프로그래밍
 * - 땃고양이
 *    - 땃고양이의 윈터
 *    - 모던 자바스크립트 딥다이브
 *
 *
 */
@Component
@RequiredArgsConstructor
public class InitDb {

    private final InitService initService;

    @PostConstruct
    public void init() {
        initService.dbInit1();
        initService.dbInit2();
    }
    
    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {

        private final EntityManager em;

        public void dbInit1() {
            Member member = createMember("땃쥐", "대전광역시", "서구", "12345");
            em.persist(member);

            Book book1 = createBook("땃쥐의 스프링", 10000, 1000);
            em.persist(book1);

            Book book2 = createBook("자바 ORM 표준 JPA 프로그래밍", 20000, 2000);
            em.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, book1.getPrice(), 1);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, book2.getPrice(), 2);
            Order order = Order.createOrder(member, createDelivery(member), orderItem1, orderItem2);
            em.persist(order);
        }

        public void dbInit2() {
            Member member = createMember("땃고양이", "서울특별시", "강남구", "54321");
            em.persist(member);

            Book book1 = createBook("땃고양이의 윈터", 20000, 2000);
            em.persist(book1);

            Book book2 = createBook("모던 자바스크립트 딥다이브", 40000, 4000);
            em.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, book1.getPrice(), 3);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, book2.getPrice(), 4);
            Order order = Order.createOrder(member, createDelivery(member), orderItem1, orderItem2);
            em.persist(order);
        }

        private Delivery createDelivery(Member member) {
            Delivery delivery = new Delivery();
            delivery.setAddress(copyAddress(member));
            delivery.setStatus(DeliveryStatus.READY);
            return delivery;
        }

        private Address copyAddress(Member member) {
            return new Address(
                    member.getAddress().getCity(),
                    member.getAddress().getStreet(),
                    member.getAddress().getZipcode()
            );
        }

        private Book createBook(String bookName, int price, int stockQuantity) {
            Book book1 = new Book();
            book1.setName(bookName);
            book1.setPrice(price);
            book1.setStockQuantity(stockQuantity);
            return book1;
        }

        private Member createMember(String memberName, String city, String street, String zipcode) {
            Member member = new Member();
            member.setName(memberName);
            member.setAddress(new Address(city, street, zipcode));
            return member;
        }

    }
}
