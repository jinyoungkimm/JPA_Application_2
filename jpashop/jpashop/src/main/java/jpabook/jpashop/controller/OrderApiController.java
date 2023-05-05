package jpabook.jpashop.controller;


import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@RestController
@RequiredArgsConstructor
public class OrderApiController { //[주문 내역]에서 주문한 [상품 정보(OrderItem,Item필요)]를
                                  // 추가로 조회하는 API

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;


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
    public List<OrderDto> ordersV3_1(
            @RequestParam(value ="offset",defaultValue = "0") int offset,
            @RequestParam(value= "limit",defaultValue = "100") int limit)
    {

        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset,limit);


       List<OrderDto> result = orders.stream()
                .map(o->new OrderDto(o))
                .collect(Collectors.toList());

        return result;

    }

    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() { // JPQL 쿼리에 new 연산자를
                                            // 사용하여 [컬렉션]을 매개변수로 넘긴 DTO의 형태로 조회

        return orderQueryRepository.findOrderQueryDtos();


    }

    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() { // V4에서 발생한 ( 1 + N ) 문제를 해결!


        return orderQueryRepository.findAllByDto_optimization();


    }

    @GetMapping("/api/v6/orders")
    public List<OrderFlatDto> ordersV6() { // V5에서는 총 2번의 SQL문으로 완성하였지만, 여기서는
                                            // 단 1번의 SQL문으로 같은 결과를 내 보겠다.
        return orderQueryRepository.findAllByDto_flat();

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

            name = order.getMember().getName();

            orderDate = order.getOrderDate();

            orderStatus = order.getOrderStatus();

            address = order.getDelivery().getAddress();

            orderItems = order.getOrderItems().stream()
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

            itemName = orderItem.getItem().getName();

            orderPrice = orderItem.getOrderPrice();

            count = orderItem.getCount();
        }
    }
}
