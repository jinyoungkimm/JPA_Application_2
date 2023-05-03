package jpabook.jpashop.service;


import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {


    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    //주문하기
    @Transactional // 주문할 때, 입력해야 할 데이터를 매개변수로 넣었다.
    public Long order(Long memberId,Long itemId,int count){

        //id로 엔티티 검색!
        Member member = memberRepository.findOne(memberId);
        Item item = itemRepository.findOne(itemId);

        //배송정보 생성
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());

        //주문상품(OrderItem) 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);// 이 메서드는 static으로 정의돼 있다.

        //주문(Order) 생성
        Order order = Order.createOrder(member, delivery, orderItem);

        //주문 저장(orderItem,Deliver 객체를 Context에 persist하지 않아도, Order 클래스에 Cascade.ALL이
        //설정돼 있어서, 자동으로 Context에 persist된다)
        orderRepository.save(order);

        return order.getId();
    }
    //주문 취소
    @Transactional
    public void cancelOrder(Long orderId){

        Order order = orderRepository.findOne(orderId);
        order.cancel();

    }

    //주문 내역
    public List<Order> findOrderSearch(OrderSearch orderSearch){

        return orderRepository.findAllByString(orderSearch);

    }

}
