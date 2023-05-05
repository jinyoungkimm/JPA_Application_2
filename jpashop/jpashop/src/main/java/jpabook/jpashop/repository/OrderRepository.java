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
        /*List<Order> resultList = entityManager.createQuery(
                        "select o from Order o " +

                                "join fetch " +
                                //@~ToOne : 무조건 fetch join으로 최적화
                                "o.member m " +

                                "join fetch " +
                                //@~ToOne
                                "o.delivery d"

                        , Order.class)
                .getResultList();*/

        // 위 쿼리와 같이, FETCH JOIN을 하지 않아도, fetch_size에 의해 최적화가 가능
        // Order안의 Member와 Dilivery에 Lazy가 걸려 있어서,
        // 조회가 안 될 것 같지만, 현재 fetch_size가 100으로 걸려 있으므로,
        // 자동으로 Proxy 객체가 초기화 된다.
        List<Order> resultList = entityManager.createQuery(
                "select o from Order o ", Order.class)
                .getResultList();

        return resultList;
    }

    public List<Order> findAllWithMemberDelivery(int offset, int limit) {


        return entityManager.createQuery(
                        "select o from Order o" +

                                // A] 컬렉션이 아닌 것!
                                " join fetch o.member m" +

                                " join fetch o.delivery d", Order.class)
                //페이징 : SQL문에 페이징을 위한 쿼리문이 날라간다.(@~ToOne 관계이므로!!)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();



    }

    public List<Order> findAllWithItem(){

            return entityManager.createQuery(
                    "select DISTINCT o from Order o" + // distinct를 넣은 이유는 아래의 주석 참조!

                            // A] 컬렉션이 아닌 것!
                            //@~~ToOne은 얼마든지 join fetch해도 된다 : fetch join해도 row수가 증가x
                            " join fetch o.member m"+
                            //@~~ToOne
                            " join fetch o.delivery d"+

  ///////////////////////////////////////////////////////////////////////////////////

                             // B] 컬렉션인 것!
                             //@~~ToMany는 join fetch해서는 안된다 -> [페이징] 이슈!
                            " join fetch o.orderItems oi"+ // Order : OrderItem = [1:다]

                            // C] 컬렉션을 통해서만 조회가 가능한 것!
                            //@~~ToOne
                            " join fetch oi.item i" ,Order.class) // Order -> OrderItem -> Item이므로
                                                                  // Order : Item = [1 : 1] 관계이다.

                    .getResultList();


    }


}
