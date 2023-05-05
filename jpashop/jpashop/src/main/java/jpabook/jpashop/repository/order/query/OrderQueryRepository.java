package jpabook.jpashop.repository.order.query;


import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public List<OrderQueryDto> findAllByDto_optimization() { //findOrderQueryDtos()에서 발생한 (1 + N)문제 해결


        List<OrderQueryDto> result = findOrders(); // 1번의 SQL문 날라감.

        List<Long> orderIds = result.stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());

        //(1+N) 문제 해결!
        // -> findOrderQueryDtos()에서처럼, 반복문으로 1개씩 1개씩 가져오지 않고
        // IN 쿼리를 사용하여, 1번의 SQL문으로 다 들고 온다.
        List<OrderItemQueryDto> orderItems = entityManager.createQuery(

                        "select" +
                                " new jpabook.jpashop.repository.order.query." +
                                "OrderItemQueryDto(oi.order.id, i.name,oi.orderPrice, oi.count)" +
                                " from OrderItem oi" +
                                " join oi.item i" +
                                " where oi.order.id IN :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();

            Map<Long, List<OrderItemQueryDto>> map =
                orderItems.stream()
                        // OrderItemQueryDto들을 같은 [OrderId]를 가진 것들로 grouping해서 Map으로 반환
                        // Map의 key는 orderId가 된다.
                .collect(Collectors.groupingBy(
                        orderItemQueryDto -> orderItemQueryDto.getOrderId())
                );

            result.forEach(o -> o.setOrderItems(map.get(o.getOrderId())));

            //총 2번의 SQL문을 날려서 조회함. ( 다음에는 단 1번의 SQL문으로 이 모든 걸 해결하는 법을 배운다)
            return result;
    }

    public List<OrderFlatDto> findAllByDto_flat() { // 총 단 1번의 SQL문으로 조회가 될 것이다.

    return entityManager.createQuery(

            "SELECT" +
                    " new jpabook.jpashop.repository.order.query." +
                    "OrderFlatDto(o.id,m.name,o.orderDate,o.orderStatus,d.address,i.name,oi.orderPrice,oi.count)" +
                    " from Order o" +
                    " join o.member m" +
                    " join o.delivery d" +
                    " join o.orderItems oi" + // 1:다 에 의해서 여기서 [데이터 뻥튀기]가 일어남.(v6를 호출해서 결과 확인해봐라)
                                              // 심지어, [페이징]도 불가능하다.
                    " join oi.item i",OrderFlatDto.class)
            .getResultList();

    }

}