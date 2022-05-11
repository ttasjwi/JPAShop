package jpa.book.JPAShop.domain.item;

import jpa.book.JPAShop.domain.Category;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.List;

@Entity
@DiscriminatorValue("B")
@Getter @Setter
public class Book extends Item {


    private String author;
    private String isbn;

    public void change(String updateName, int updatePrice, int updateStockQuantity, String updateAuthor, String updateIsbn) {
        super.setName(updateName);
        super.setPrice(updatePrice);
        super.setStockQuantity(updateStockQuantity);
        this.setAuthor(updateAuthor);
        this.setIsbn(updateIsbn);
    }
}
