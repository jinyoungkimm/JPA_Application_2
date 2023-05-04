package jpabook.jpashop.repository.order.simplequery;


import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;


// OrderSimpleApiController 클래스 내에 선언된 static class SimpleOrderDTO 클래스를 여기서 따로 뺀 이유
// -> 이번 강의 시간에, OrderSimpleApiController에서 orderRepository.findOrderDTOs()라는 새로운 메서드를 사용하였다.
// OrderRepository에서 해당 메서드를 구현을할 때, SimpleOrderDTO 클래스를 사용하여야 하는데,
// static으로 선언돼 있으므로, SimpleOrderDTO 클래스를 사용할 때 아래와 같이 적어 줘야 한다.
// [OrderSimpleApiController.SimpleOrderDTO]
// -> 이렇게 되면, Repository에서 Controller를 [의존]해버리는 치명적인 결과가 되버린다.
// 그래서 걍 이렇게 따로 클래스 파일을 만들어 주었다.
@Data
public class OrderSimpleQueryDTO {

    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;

    public OrderSimpleQueryDTO(Long orderId, String name, LocalDateTime
            orderDate, OrderStatus orderStatus, Address address) {

        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;
    }



}
