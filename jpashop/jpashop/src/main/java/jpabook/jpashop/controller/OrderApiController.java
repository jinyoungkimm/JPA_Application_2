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

//            for (OrderItem orderItem : orderItems) {
//                orderItem.getItem().getName(); // Lazy 객체 강제 초기화
//            }
            orderItems.stream().forEach(o->o.getItem().getName()); // 위 for문을 Lamda식으로 변경
        }

        return all;

    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2(){ //[도메인 엔티티]를 DTO로 [한 번] 감싸서 전달.

        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        // Lazy 객체 강제 초기화는 OrderDto 클래스에서 구현해 놓음.
        // 이 반복문을 통해 그 구현이 실행됨으로서 초기화가 일어남.
        // 이 시점에는 아직 orderRepository에서 OrderItem, Item에 대한 fetch Join JPQL이 없으므로,
        // 아래 람다에서 반복문이 일어 날때마다 조회하는 SQL문이 날라간다.(N+1문제 V3에서 Fetch Join으로 최적화 할 예정)
        // 그러나, 컬렉션을 fetch join할 때 주의해야 할 부분이 있다고 말씀하심(추후에 설명 예정이라고 함)
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

        // private List<OrderItem> orderItems; // OrderItem는 [도메인 엔티티]이다.
        // 우리는 [도메인 엔티티]를 직접 전달하지 않기 위헤서, DTO로 [한 번] 감싸서 보내면 괜찮겠지 싶어서 보냈지만,
        // DTO 안에서 조차, [도메인 엔티티]가 있으면 안된다(이를 "DTO가 [도메인 엔티티]에 의존하고 있다"라고 표현)
        // 전달하는 DTO에서 조차, [도메인 엔티티]를 의존하고 있으면 안된다.
        // 왜냐하면, 클라이언트에게 이 DTO가 뿌려지게 되면, OrderItem [도메인 엔티티]가 그대로 노출되기 때문.
        // 결론 : 전달되는 DTO는 그 어떠한 [도메인 엔티티]도 의존하고 있으면 안된다.
        // [도메인 엔티티]에 대한 의존이 없어지도록, 해당 [도메인 엔티티]를 별도의 DTO를 생성해서, 다시 한번
        // 감싸서 보내야 함.(OrderItem을 OrderItemDto 클래스로 Wrapping 하자)
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


}
