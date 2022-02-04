package jpa.book.JPAShop;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {

    @GetMapping("hello") // get 방식으로 요청이 들어왔을 때
    public String hello(Model model) {
        model.addAttribute("data", "hello!"); // "data"에 "hello!"를 넣어서 view에 넘김
        return "hello";
    }
}
