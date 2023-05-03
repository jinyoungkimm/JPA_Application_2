package jpabook.jpashop.api;


import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.AbstractAuditable_;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * [주문 + 배송정보 + 회원]을 조회하는 Rest API
 *
 * 아래 모두, Order를 기준으로 @~ToOne 관계이다(단, 방향은 양방향으로 설정을 하였지만, 이는 JPA의 설명을 위한 설정이며,
 * 단방향으로 설정돼 있다고 생각을 하고 각 엔티티를 [조회]해오자.)
 * Order 엔티티 안의 연관 관계 객체들
 *  1] Order -> Member
 *  2] Order -> Delivery
 *  -> Order 객체만 [조회]를 하면, cascade.all에 의해  OrderItem,Delivery 객체는 DB로부터 자동으로 조회된다.
 *
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderSimpleApiController {

    // 이 Controller는 DB에 있는 정보를 [조회]해와서 뿌리는 API용이다.
    // 즉, 회원 가입 등의 기능을 여기서 필요로 하지 않기에 Service 계층을 거치지 않아도 된다.
    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1(){

        //도메인 엔티티를 반환하는 안 좋은 API이다.
        List<Order> findOrders = orderRepository.findAllByString(new OrderSearch());

        //Lazy가 걸린 엔티티를를 사용함으로써 Proxy 객체를 초기화 시켜서 Lazy가 걸린 연관관계 객체를 조회하는 방법도 있다.
        for(Order order : findOrders){
            order.getMember().getName(); //  Order::Member이 강제 초기화!
            order.getDelivery().getAddress(); // Order::Delivery이 강제 초기화!
        }
        return findOrders; // Order::Member과 Order::Delivery만을 반환할 수 있게 되었다.

    }

    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDTO> orderV2(){ // 컬렉션을 [직접] 반환하는 안 좋은 API이다.

        List<Order> findOrders = orderRepository.findAllByString(new OrderSearch());

        //이 과정에서 Lazy로 인한 너무 많은 SQL문이 날라간다. 아래의 SimpleOrderDTO 클래스 참조!
        // 반복문이 일어날 때마다 Select문이 날라간다(N+1문제).
        List<SimpleOrderDTO> resultList = findOrders.stream()
                .map(o -> new SimpleOrderDTO(o))
                .collect(Collectors.toList());

        return resultList;

    }
    @Data
    public static class SimpleOrderDTO{

        // 우리는 Order 객체를 반환을 하되, Order::Member과 Order::Delivery 내의 모든 필드값을 API로 반환하는 것이 아니라,
        // 미리 클라이언트와 협의를 해서 정해 놓은,
        // API 결과 스펙을 아래와 같이 정하여, 클라이언트가 필요로하는 정보만을 추출하여 반환을 할 것이다.
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDTO(Order order) {

            //이 과정에서 Lazy가 걸린 member, deliver가 사용이 되면서, Proxy가 초기화 되는 과정에서 select문이 날라간다.
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getOrderStatus();
            address = order.getDelivery().getAddress();
        }

    }

    //v1,v2의 공통적인 문제점 : (N+1) 문제
    //Solution : Fetch Join

    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDTO> orderV3() {

        //fetch join을 사용하여 Order 조회!
        List<Order> findOrders = orderRepository.findAllWithMemberDelivery();// 새로 메서드를 만들어서 fetch join 사용!

        // 미리 정해진 API 결과 스펙을 반환하기 위하여, DTO에 정해진 스펙대로 필드값들을 넣어 준다.
        List<SimpleOrderDTO> resultList = findOrders.stream()
                .map(o -> new SimpleOrderDTO(o))
                .collect(Collectors.toList());

        return resultList;

    }

}
