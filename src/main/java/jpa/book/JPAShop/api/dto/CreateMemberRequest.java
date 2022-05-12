package jpa.book.JPAShop.api.dto;

import jpa.book.JPAShop.domain.Address;
import jpa.book.JPAShop.domain.Member;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class CreateMemberRequest {
    @NotEmpty
    private String name;
    private String city;
    private String street;
    private String zipcode;

    public Member toEntity() {
        Member member = new Member();
        member.setName(name);
        member.setAddress(new Address(city,street, zipcode));
        return member;
    }
}

