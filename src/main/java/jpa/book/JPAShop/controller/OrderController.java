package jpa.book.JPAShop.controller;

import jpa.book.JPAShop.domain.Member;
import jpa.book.JPAShop.domain.item.Item;
import jpa.book.JPAShop.service.ItemService;
import jpa.book.JPAShop.service.MemberService;
import jpa.book.JPAShop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final MemberService memberService;
    private final ItemService itemService;

    @GetMapping("/create")
    public String createForm(Model model) {
        List<Member> members = memberService.findMembers();
        List<Item> items = itemService.findItems();
        model.addAttribute("members", members);
        model.addAttribute("items", items);

        return "orders/orderCreateForm";
    }

    @PostMapping
    public String order(
            @RequestParam("memberId") Long memberId,
            @RequestParam("itemId") Long itemId,
            @RequestParam("count") int count) {
        orderService.order(memberId, itemId, count);
        return "redirect:/";
    }
}
