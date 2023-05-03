package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.service.ItemService;
import jpabook.jpashop.service.MemberService;
import jpabook.jpashop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
@RequiredArgsConstructor
public class OrderController { // [주문 기능]에 대한 controller

    //[주문 기능] 페이지들을 보고, 왜 아래와 같은 객체가 필요한지 확인하라.
    private final OrderService orderService; // 주문 정보
    private final MemberService memberService; // 주문 회원
    private final ItemService itemService; // 주문 상품


    @GetMapping("/order")
    public String createForm(Model model){ // [상품 주문]


        //주문하는 페이지를 보면 왜 아래 2개가 필요한 지 알 것이다.
        List<Member> members = memberService.findMembers();
        List<Item> items = itemService.findItems();

        model.addAttribute("members",members);
        model.addAttribute("items",items);

        return "order/orderForm";

    }


    @PostMapping(value = "/order") // [상품 주문] 페이지에서 주문 관련 데이터가 넘어 온다.
    public String order(@RequestParam("memberId") Long memberId,
                        @RequestParam("itemId") Long itemId, @RequestParam("count") int count) {
        orderService.order(memberId, itemId, count);
        return "redirect:/orders";
    }

    @GetMapping(value = "/orders") //[주문 내역]
    public String orderList(@ModelAttribute("orderSearch") OrderSearch
                                    orderSearch, Model model) {
        List<Order> orders = orderService.findOrderSearch(orderSearch);
        model.addAttribute("orders", orders);
        return "order/orderList";
    }


    @PostMapping(value = "/orders/{orderId}/cancel") // [주문 취소]
    public String cancelOrder(@PathVariable("orderId") Long orderId) {

        orderService.cancelOrder(orderId);

        return "redirect:/orders";
    }


}






