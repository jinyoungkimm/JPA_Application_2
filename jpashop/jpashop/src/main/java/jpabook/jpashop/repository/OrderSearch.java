package jpabook.jpashop.repository;


import jpabook.jpashop.domain.OrderStatus;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter

public class OrderSearch { // [주문 검색] 페이지 기능 구현!
    // ppt 65페이지를 보면 알겠지만, 이 부분은 동적 쿼리가 필요하다.
    // OrderStatus(주문 상태)에 따라, 검색 결과가 다르다.
    private String memberName; // [주문 회원]
    private OrderStatus orderStatus;// [주문 상태]


}
