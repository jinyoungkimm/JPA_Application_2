package jpabook.jpashop.controller;


import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * V1. 엔티티 직접 노출
 * - Hibernate5Module 모듈 등록, LAZY=null 처리
 * - 양방향 관계 문제 발생 -> @JsonIgnore
 */

/**
 * 여태껏 엄청난 착각을 하였다.
 * order.getMember() : Lazy Member 초기화 x
 * order.getMember().getName() : Lazy Member 초기화 o
 * -> Lazy 객체의 메서드를 호출해야지, 초기화가 되는 거였다.
 */

@RestController
@RequiredArgsConstructor
public class OrderApiController { //[주문 내역]에서 주문한 [상품 정보(OrderItem,Item필요)]를
                                  // 추가로 조회하는 API

    private final OrderRepository orderRepository;


    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1(){ // [도메인 엔티티]를 반환하고 있는 안 좋은 예시.

        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {

            order.getMember().getName(); // Lazy 객체 강제 초기화
            order.getDelivery().getAddress(); // Lazy 객체 강제 초기화

            List<OrderItem> orderItems = order.getOrderItems();

            orderItems.stream().forEach(o->o.getItem().getName()); // 위 for문을 Lamda식으로 변경
        }

        return all;

    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2(){ //[도메인 엔티티]를 DTO로 [한 번] 감싸서 전달.

        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        List<OrderDto> all = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());

        return all;
    }



    @Data
    static class OrderDto { // API의 결과 스펙
        private Long orderId;
        private String name;
        private LocalDateTime orderDate; //주문시간
        private OrderStatus orderStatus;
        private Address address;

        private List<OrderItemDto> orderItems;// OrderItemDto : 아래에 구현함
        public OrderDto(Order order) {

            orderId = order.getId();

            name = order.getMember().getName(); // Lazy 객체 강제 초기화

            orderDate = order.getOrderDate();

            orderStatus = order.getOrderStatus();

            address = order.getDelivery().getAddress(); // Lazy 객체 강제 초기화

            orderItems = order.getOrderItems().stream() // Lazy 객체 강제 초기화
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(toList());
        }
    }
    @Data
    static class OrderItemDto {
        private String itemName;//상품 명
        private int orderPrice; //주문 가격
        private int count; //주문 수량
        public OrderItemDto(OrderItem orderItem) {

            itemName = orderItem.getItem().getName();  // Lazy 객체 강제 초기화

            orderPrice = orderItem.getOrderPrice();

            count = orderItem.getCount();
        }
    }

    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() { // V2에서 생겼던, (N+1)문제를 FETCH JOIN으로 해결!!
                                       // 컬렉션 사용 시, 생기는 [데이터 중복]문제도 해결함

        List<Order> orders = orderRepository.findAllWithItem(); // fetch join을 사용하여 새로 정의한 메서드

       List<OrderDto> result = orders.stream()

                .map(o->new OrderDto(o))
                .collect(Collectors.toList());

        return result;

    }

    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_1() { // [1:다] 에서 다(컬렉션) fetch join 시, 생기는 페이지 이슈를 해결

        List<Order> orders = orderRepository.findAllWithMemberDelivery(); // @~ToOne 관계의 객체만 존재
        //이거에 대해서, 페이징을 하여도 성능 이슈가 안 생김
        // 왜냐하면, [1:다] 관계에서와는 달리, DB에서 페이징 처리를 해서 가져오기 때문([데이터 중복]이 없기 때문)!

        //여기서 Order 객체를 다른 객체(OrderItem, Item)에 대한 조회가 일어나는데
        // Lazy인 관계로 (N+1) 문제 발생생
       List<OrderDto> result = orders.stream()
                .map(o->new OrderDto(o))
                .collect(Collectors.toList());

        return result;

    }
}
