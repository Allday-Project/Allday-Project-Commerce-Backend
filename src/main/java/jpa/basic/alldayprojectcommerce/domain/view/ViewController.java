package jpa.basic.alldayprojectcommerce.domain.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ViewController {

    @GetMapping("/")
    public String index(Model model) {
        // products는 추후 서비스 연동 시 주입
        return "index";
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        return "signup";
    }

    @GetMapping("/cart")
    public String cart(Model model) {
        // cartItems, totalPrice는 추후 서비스 연동 시 주입
        return "cart";
    }

    @GetMapping("/mypage")
    public String mypage(Model model) {
        // user 정보는 추후 서비스 연동 시 주입
        return "mypage";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        // orderGroups는 추후 서비스 연동 시 주입
        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        // order 정보는 추후 서비스 연동 시 주입
        return "order-detail";
    }
}
