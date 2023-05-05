package jpabook.jpashop.repository.order.query;


import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
//@EqualsAndHashCode(of = "orderId")
    public class OrderQueryDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate; //주문시간
        private OrderStatus orderStatus;
        private Address address;

        // new 연산자 매개변수에 컬렉션을 직접 넣지는 못한다.
        // new 연산자에서 매개변수로 컬렉션을 [직접] 넣지 못하면,
        // 그 컬렉션을 [다른 곳에서] [별개의 DTO]로 감싸는 [후작업]을 해주면 그만!
        private List<OrderItemQueryDto> orderItems;

       // new 연산자 매개변수에 컬렉션을 직접 넣지는 못하므로, 매개변수에 orderItem는 넣지 않음.
       // 만약 넣게 되면, [데이터 중복] 문제 발생하기 때문!
       // -> OrderQueryDto : orderItems = [1 : 다]이므로, DB에서 OrderQueryDto이 중복되어서
       // 애플리케이션으로 날라 온다.
       // 그럼 Context에서는 OrderQueryDto를 가리키는 2개의 참조 변수가 저장이 되는 [데이터 중복] 문제 발생생

        public OrderQueryDto(Long orderId, String name, LocalDateTime orderDate,
                             OrderStatus orderStatus, Address address) {
            this.orderId = orderId;
            this.name = name;
            this.orderDate = orderDate;
            this.orderStatus = orderStatus;
            this.address = address;
        }
    }




