package jpa.book.JPAShop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

@Embeddable
@Getter // 값 타입은 Setter를 두면 안 됨.
public class Address {

    private String city;
    private String street;
    private String zipcode;

    // JPA 스펙상 객체 생성 시 리플렉션과 같은 기술을 사용할 수 있으므로 기본 생성자는 열어둠
    protected Address() {}

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
