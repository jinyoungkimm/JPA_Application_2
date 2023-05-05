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

    public List<Order> findAllWithItem(){

            return entityManager.createQuery(
                    "select DISTINCT o from Order o" + // distinct를 넣은 이유는 아래의 주석 참조!

                            " join fetch o.member m"+

                            " join fetch o.delivery d"+

                            " join fetch o.orderItems oi"+ // OrderItem은 컬렉션(Collection)으로 정의돼 있다.
                                                           // [데이터 중복]문제 발생 : DISTICT로 해결!

                            // [애플리케이션]에서 쿼리 결과를 들고 와서, [애플리케이션] 계층에서
                            // [데이터 중복]를 지워줌.
                            // (DB 사이드에서는 한 ROW가 완전히 똑같아야만 DISTINCT로 동작해서 중복이 지므로
                            //  DISTINCT를 사용하여도 [데이터 중복] 문제가 해결이 안 됨)
                            // 구체적으로는 JPA(하이버네이트)가 조회돼 Context에 캐싱된 Order 객체의 id를 보고
                            // 그 id가 같은 Order 객체에 중복 저장(정확히는,Order객체의 참조값)하지 않는다.
                            // !!하이버네이트 6.x.x부터는 DISTINCT를 넣지 않아도 [자동]으로 데이터 중복 문제를 해결
                            " join fetch oi.item i" ,Order.class)

                    .getResultList();

            // 이렇게 fetch join과 distinct로 [1+N] 문제와 [데이터 중복] 문제를 해결하였다.
            // 그러나 여기에서 치명적인 단점이 하나 있다.
            // -> [페이징이 불가능해진다], 즉 [1:다]를 [Fetch Join]하는 순간 [페이징]이 불가능해진다.
            // 게시물 426 참조!
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
