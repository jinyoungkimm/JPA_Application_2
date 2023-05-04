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

                                "o.member m " +

                                "join fetch " +

                                "o.delivery d"

                        , Order.class)
                .getResultList();

        return resultList;
    }

    //특정 API 스펙에 맞춘 JPQL 작성 방식이므로, orderRepository에 이 메서드를 두는 것은 옳지 않아서
    //OrderSimpleQueryRepository 클래스를 만들어서 거기 메서드에 새로 넣음.

  /*  public List<OrderSimpleQueryDTO> findOrderDTOs() {

       return  entityManager.createQuery(

               "select" +

                       // new OrderSimpleQueryDTO(매개변수) 연산자를 통해 JPQL에서 바로 DTO로 변환해서 반환받을 수가 있다.
                       // lazy가 걸려있는 member나 delivery도 즉시 조회가 된다(정확히는 member,delivery 객체가 조회되는 것이 아니라,
                       // 그 안의 Projection 대상으로 지정된 필드들만 DB에서 가져온다)
                       // -> Fetch Join을 쓰지 않아도 이와 같이 필요한 필드값만을 Projectino으로 등록해서 조회하여
                       // SQL문을 딱 1번만 날릴 수가 있다.
                " new jpabook.jpashop.repository.OrderSimpleQueryDTO(o.id, m.name, o.orderDate, o.orderStatus, d.address)" +

                " from Order o" +

                " join o.member m" +

                " join o.delivery d", OrderSimpleQueryDTO.class)

                .getResultList();

    }*/


}
