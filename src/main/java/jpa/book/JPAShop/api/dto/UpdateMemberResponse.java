package jpa.book.JPAShop.api.dto;

import jpa.book.JPAShop.domain.Member;
import lombok.Data;

@Data
public class UpdateMemberResponse {

    private Long id;
    private String name;
    private String city;
    private String street;
    private String zipcode;

    public UpdateMemberResponse(Member updatedMember) {
        this.id = updatedMember.getId();
        this.name = updatedMember.getName();
        this.city = updatedMember.getAddress().getCity();
        this.street = updatedMember.getAddress().getStreet();
        this.zipcode = updatedMember.getAddress().getZipcode();
    }
}