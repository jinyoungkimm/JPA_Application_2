package jpabook.jpashop.repository.order.simplequery;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

// 이 클래스 파일을 별도로 만든 이유는 https://jbluke.tistory.com/421 참조
@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {

    private final EntityManager entityManager;

    public List<OrderSimpleQueryDTO> findOrderDTOs() {

        return  entityManager.createQuery(

                        "select" +

                                " new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDTO(o.id, m.name, o.orderDate, o.orderStatus, d.address)" +

                                " from Order o" +

                                " join o.member m" +

                                " join o.delivery d", OrderSimpleQueryDTO.class)

                .getResultList();

    }


}
