package jpabook.jpashop.controller;


import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * V1. 엔티티 직접 노출
 * - Hibernate5Module 모듈 등록, LAZY=null 처리
 * - 양방향 관계 문제 발생 -> @JsonIgnore
 */

@RestController
@RequiredArgsConstructor
public class OrderApiController { //[주문 내역]에서 주문한 상품 정보(OrderItem,Item필요)를
                                  // 추가로 조회하는 API

    private final OrderRepository orderRepository;


    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1(){

        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {

            order.getMember().getName(); // Lazy 객체 강제 초기화
            order.getDelivery().getAddress(); // Lazy 객체 강제 초기화

            List<OrderItem> orderItems = order.getOrderItems(); // Lazy 객체 강제 초기화

//            for (OrderItem orderItem : orderItems) {
//                orderItem.getItem().getName(); // Lazy 객체 강제 초기화
//            }
            orderItems.stream().forEach(o->o.getItem().getName()); // 위 for문을 Lamda식으로 변경
        }

        return all;

    }






}
