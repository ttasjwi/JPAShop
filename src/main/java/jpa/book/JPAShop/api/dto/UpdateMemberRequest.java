package jpa.book.JPAShop.api.dto;


import jpa.book.JPAShop.domain.Address;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class UpdateMemberRequest {
    @NotEmpty
    private String name;
    private String city;
    private String street;
    private String zipcode;

    public Address getAddress() {
        return new Address(city, street, zipcode);
    }
}
