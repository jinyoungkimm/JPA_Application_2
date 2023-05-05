package jpabook.jpashop.repository;


import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDTO;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {  //Order 객체에 대한 Repository


    private final EntityManager entityManager;

    public void save(Order order){

        entityManager.persist(order);

    }


    public Order findOne(Long id){

        return entityManager.find(Order.class,id);
    }


    public List<Order> findAllByString(OrderSearch orderSearch) {
        //language=JPAQL
        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }
        TypedQuery<Order> query = entityManager.createQuery(jpql, Order.class)
                .setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }

    public List<Order> findAllWithMemberDelivery() {

        //Fetch Join 사용하여 1번의 SQL문으로 [Member + Deliver] 조회
        List<Order> resultList = entityManager.createQuery(
                        "select o from Order o " +

                                "join fetch " +
                                //@~ToOne : 무조건 fetch join으로 최적화
                                "o.member m " +

                                "join fetch " +
                                //@~ToOne
                                "o.delivery d"

                        , Order.class)
                .getResultList();

        return resultList;
    }

    public List<Order> findAllWithItem(){

            return entityManager.createQuery(
                    "select DISTINCT o from Order o" + // distinct를 넣은 이유는 아래의 주석 참조!

                            //@~~ToOne은 얼마든지 join fetch해도 된다 : fetch join해도 row수가 증가x
                            " join fetch o.member m"+
                            //@~~ToOne
                            " join fetch o.delivery d"+

                             //@~~ToMany는 join fetch해서는 안된다 -> [페이징] 이슈!
                            " join fetch o.orderItems oi"+ // Order : OrderItem = [1:다]

                            //@~~ToMany
                            " join fetch oi.item i" ,Order.class) // Order -> OrderItem -> Item이므로
                                                                  // Order : Item = [1 : 다] 관계이다.

                    .getResultList();


    }

}
