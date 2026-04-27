package jpa.basic.alldayprojectcommerce.domain.view;

import jakarta.servlet.http.Cookie;
import jpa.basic.alldayprojectcommerce.common.security.auth.AuthConstants;
import jpa.basic.alldayprojectcommerce.common.security.cookie.CookieUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final CookieUtils cookieUtils;

    @GetMapping("/")
    public String index(Model model) {
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
        return "cart";
    }

    @GetMapping("/mypage")
    public String mypage(Model model) {
        return "mypage";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable String id, Model model) {
        return "order-detail";
    }

    @GetMapping("/checkout")
    public String checkout(Model model) {
        return "checkout";
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie accessToken = cookieUtils.deleteCookie(AuthConstants.ACCESS_TOKEN);
        Cookie refreshToken = cookieUtils.deleteCookie(AuthConstants.REFRESH_TOKEN);
        
        response.addCookie(accessToken);
        response.addCookie(refreshToken);
        
        return "redirect:/";
    }

    @GetMapping("/admin/consultations")
    public String adminConsultations(Model model) {
        return "admin-consultation";
    }
}
