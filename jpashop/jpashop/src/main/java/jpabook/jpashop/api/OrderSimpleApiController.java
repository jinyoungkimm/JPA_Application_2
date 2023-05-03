package jpabook.jpashop.api;


import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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





}
