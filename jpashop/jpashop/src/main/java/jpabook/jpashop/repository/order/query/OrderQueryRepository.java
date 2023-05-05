package jpabook.jpashop.repository.order.query;


import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Repository
@RequiredArgsConstructor

public class OrderQueryRepository {

    private final EntityManager entityManager;

    public List<OrderQueryDto> findOrderQueryDtos(){

        List<OrderQueryDto> results = findOrders();

        // findOrders()에서 OrderQueryDto로 바로 컬렉션(OrderItem)을 넘기지 못했으므로,
        // 여기서 컬렉션(OrderItem)을 [별도의 DTO]인 OrderItemDTO로 감싸서
        // OrderQueryDto에 넣어 주는 [후작업]을 하자!
        results.forEach(o ->{
                    // findOrderItems(orderId)라는 별도의 메서드를 만들어서
                    // 별도의 JPQL을 작성하여, OrderItem을 조회하자!
                    // ToOne은 row수가 증가하지 않기 때문에 join으로 최적화가 쉬운데
                    // ToMany는 [데이터 뻥튀기] 때문에 한 번에 같이 join으로 최적화하기 어려우므로
                    // 별도의 메서드로 뽑는 방법외에는 길이 없다.
                    // -> 그러나 반복문이 돌 때마다, findOrderItem(orderId)에 의해 1번씩 SQL문이 날라가기에
                   // (1 + N)의 문제가 터진다(이 문제는 다음 시간에 최적화 할 거임)
                    List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
                    o.setOrderItems(orderItems);
                }
        );
        return results;
    }


    // 만약, OrderQueryDto 대신, OrderApiController에 있던, OrderDto를 List<OrderDto>로 사용하게 되면
    // OrderQueryController가 OrderApiRepository를 참조하게 되어 버리는 문제가 발생.
    // OrderApiController -> OrderQueryRepository && OrderQueryRepository ->  OrderApiController의 결과
    // OrderApiController <-> OrderQueryRepository 순환 참조 문제가 발생한다.
    public List<OrderQueryDto> findOrders() {

         return entityManager.createQuery(

           "select" +
                   //JPQL이라도, NEW 매개변수에 [컬렉션](OrderItem)을 [직접] 넣지는 못한다.
                   " new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id,m.name,o.orderDate,o.orderStatus,d.address) " +
                   " from Order o " +
                   " join o.member m" +
                   " join o.delivery d", OrderQueryDto.class)
           .getResultList();

    }



    private List<OrderItemQueryDto> findOrderItems(Long orderId) {

        return entityManager.createQuery(

                "select" +
                        " new jpabook.jpashop.repository.order.query." +
                        "OrderItemQueryDto(oi.order.id, i.name,oi.orderPrice, oi.count)" +
                        " from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id = :orderId",OrderItemQueryDto.class)
                .setParameter("orderId",orderId)
                .getResultList();

    }

}
