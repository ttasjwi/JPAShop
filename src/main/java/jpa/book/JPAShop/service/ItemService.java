package jpa.book.JPAShop.service;

import jpa.book.JPAShop.controller.BookUpdateDto;
import jpa.book.JPAShop.domain.item.Book;
import jpa.book.JPAShop.domain.item.Item;
import jpa.book.JPAShop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    @Transactional
    public void updateBook(Long itemId, BookUpdateDto updateDto) {
        Book findBook = (Book) itemRepository.findOne(itemId);
        findBook.change(
                updateDto.getName(),
                updateDto.getPrice(),
                updateDto.getStockQuantity(),
                updateDto.getAuthor(),
                updateDto.getIsbn()
        );
    }
}
