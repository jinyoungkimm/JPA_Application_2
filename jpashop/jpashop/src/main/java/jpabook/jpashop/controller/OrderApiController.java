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
import org.springframework.web.bind.annotation.RequestParam;
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

        // IN 쿼리를 사용해서,  DB에서 OrderItem 객체 조회해서 옴
        // default_batch_fetch_size = 10이면
        // "IN 쿼리 안의 데이터 개수를 10개로 할거다"라는 뜻!
        // 게시물 428 참조!
        public OrderDto(Order order) {

            orderId = order.getId();

            name = order.getMember().getName();

            orderDate = order.getOrderDate();

            orderStatus = order.getOrderStatus();

            address = order.getDelivery().getAddress();

            // IN 쿼리를 사용해서, DB에서 Item 객체 조회해서 옴
            //default_batch_fetch_size = 10이면
            // "IN 쿼리 안의 데이터 개수를 10개로 할거다"라는 뜻!
            // 게시물 428 참조!
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
        //1] @~ToOne 관계에 있는 객체를 [fetch join]으로 일단 먼저 조회한다.
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset,limit);

        // OrderDto 클래스 내에서 Lazy가 걸린 @ToMany 엔티티(컬렉션)에 대해서
        // (N + 1) 문제를 일으키지 않고, IN 쿼리로 조회를 1번의 SQL로 함
        // 구체적인 것은 게시물 428 참조!
       List<OrderDto> result = orders.stream()
                .map(o->new OrderDto(o))
                .collect(Collectors.toList());

        return result;

    }

    //V3는 SQL문이 총 1번 나가고, V3.1은 SQL문이 총 3번 나간다.
    //그럼 V3가 더 좋은 것일까??
    //그렇지만은 않다. V3을 실행해 보고, DB Table을 보면 알겠지만, 중복된 데이터가 너무 많다.
    //DB는 그 중복된 데이터를 모두 애플리케이션으로 보내야 하므로, 네트워크 비용이 발생한다.
    //그러나 V3.1은 총 3번의 SQL문이 나가지만 [데이터가 중복없이 최적화] 되어서 나가기 때문에
    //보내지는 데이터의 총량이 적으므로, 그에 따른 비용 절감이 일어난다.

    // 중복되는 데이터 양이 너~~~~무 많을 때는 V3.1이 좋고
    // 중복되는 데이터 양이 그렇게 심하게 차이가 나지 않을 때는, 요즘에는 네트워킹이 매우 잘 되었있으므로
    // 어느 버전으로 해도 별 차이가 없을 것이다.
    // 그러나 V3에서는 [페이징] 이슈가 생기므로, 일반적으로 V3.1을 사용 권장!

}
