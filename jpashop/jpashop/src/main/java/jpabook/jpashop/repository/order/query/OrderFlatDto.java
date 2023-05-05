package jpabook.jpashop.repository.order.query;


import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderFlatDto {

    /**
     *  OrderQueryDto 중 일부 속성들
     */
    private Long orderId;
    private String name;
    private LocalDateTime orderDate; //주문시간
    private OrderStatus orderStatus;
    private Address address;

    private List<OrderItemQueryDto> orderItems;

///////////////////////////////////////////////////////
    /**
     * OrderItemQueryDto 중 일부 속성들
     */
    private String itemName;//상품 명
    private int orderPrice; //주문 가격
    private int count; //주문 수량
    ///////////////////////////////////////////////////////

    // DB에서 위 형태의 DTO로 한 방에 가져올 것이다(필요한 모든 필드값들이 들어 있다.)

    // Order -> OrderItem -> Item, 이 3개를 JOIN을 통해 한 번에 들고 오도록, JPQL을 작성할 것이다.


    public OrderFlatDto(Long orderId, String name, LocalDateTime orderDate,
                        OrderStatus orderStatus, Address address, String itemName, int orderPrice, int
                                count) {
        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;
        this.itemName = itemName;
        this.orderPrice = orderPrice;
        this.count = count;
    }

}
