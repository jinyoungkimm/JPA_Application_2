package jpabook.jpashop.api;


import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDTO;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    //보통, Controller는 Service와 의존관계를 가진다.
    //  [Controller] -> [Service] -> [Repository]와 같이 보통 [관계(Relation)]을 정의를 한다.
    // -> [Repository]->[Service] , [Repository]->[Controller] 또는 [Service] -> [Controller] 와 같이 반대로 의존관계를 가지게 해서는 절대 안됨.
    // 그러나 이산 수학에서의 [관계(Ralation)]를 생각해 보면,
    // [Controller] -> [Repository]는 아무런 문제가 없다.
    // 결론 : 꼭, Controller 계층에서 Service 계층만을 의존하는 것은 아니다.
    private final OrderRepository orderRepository;

    private OrderSimpleQueryRepository orderSimpleQueryRepository;

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
        // 반복문이 일어날 때마다 Select문이 날라간다(N+1문제)
        List<SimpleOrderDTO> resultList = findOrders.stream()
                .map(o -> new SimpleOrderDTO(o))
                .collect(Collectors.toList());

        return resultList;

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

    @Data
    static class SimpleOrderDTO {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate; //주문시간
        private OrderStatus orderStatus;
        private Address address;
        public SimpleOrderDTO(Order order) {

            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getOrderStatus();
            address = order.getDelivery().getAddress();
        }
    }

    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDTO> orderV4() { //  JPA에서 DTO를 바로 조회하기
                                                 //  즉, JPQL문을 실행하면, 바로 DTO 형태로 뱉게 만든다.

        return orderSimpleQueryRepository.findOrderDTOs();

    }
    // 419 게시물 참조
    //V3, V4의 차이점
    //-> V4는 Projection 대상을 [API 결과 스펙]에 맞게, 필요한 것만 지정을 해 주었기 때문에 DB에서 서버로 데이터를 날릴 때 데이터 량이
    //   객체의 모든 필드들을 서버로 받는 V3보다 훨씬 적다.
    //   고로, 네트워크 비용이 V4가  좋다.(요즘에는 네트워크가 좋아져서 차이가 미미)

    // -> V3는 DB에서 [객체]를 조회한 것이기에, Context에 객체들이 캐쉬되어 있다.
    //    고로, JPA의 Dirty Checking과 setter를 통해 객체의 [변경]이 가능하다.
    //    그러나 V4는 객체를 DB로부터 넘겨 받은 것이 아니라, new 연산자를 통해서 DTO 객체를 넘겨 받은 것이기에
    //    객체의 [변경]이 불가능 하다.


    //V3, V4의 공통점 : 둘다 SQL문이 1번만 나간다.
    //V3 : FETCH JOIN으로 Lazy의 (N+1) 문제 극복
    //V4 : FETCH JOIN은 사용하지 않았지만, JPQL문에서 new 연산자를 통해 필요한 필드들만 Projcetion으로 지정을 해서 SQL문을 1번만
    // 날라게 할 수도 있다.(rderRepository.findOrderDTOs() 참조)
}
