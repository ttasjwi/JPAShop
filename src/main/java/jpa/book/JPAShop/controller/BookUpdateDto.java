package jpa.book.JPAShop.controller;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BookUpdateDto {

    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

    private String author;
    private String isbn;
}
