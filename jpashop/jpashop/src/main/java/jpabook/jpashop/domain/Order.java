package jpabook.jpashop.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ORDERS")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id@GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id")
    private Member member;


    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id") // 1:1 관계에서는 연관 관계 주인이 어디에 있어도 상관X.
    private Delivery delivery;        // Tip으로써, Order과 Delivery 중 더 많이 접근 하는 클래스에 연관 관계 주인을
                                      // 두는 것을 추천. 왜냐하면, Delivery를 통해 Order를 탐색하는 경우는 잘 없다.
    private LocalDateTime orderDate; //주문 시간

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    // 연관 관계 편의 메서드(양방향 일때 의미가 있음)
    public void setMember(Member member){

        this.member = member;
        member.getOrders().add(this); // 이걸 안 해줘도, DB 입장에서는 저장/조회/업데이트에 전혀 문제 없다.

    }

    public void addOrderItem(OrderItem orderItem)
    {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);

    }

    public void setDelivery(Delivery delivery){
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    // Order 객체의 생성 메서드
    // Order 객체와 같이 많이 연관 관계 객체를 세팅해줘야 하고, 많은 필드값을 세팅해줘야 하는 객체의 생성은
    // 아래와 같이 생성 편의 메서드를 만들어 놓으면 좋다.
    // 왜냐하면, 만약 Order 객체에 뭔가가 문제가 생겨, 해결하려고 할 때,
    // createOrder(...) 부분[만]을 찾아서 보면 되기 때문!
    public static Order createOrder(Member member,Delivery delivery,OrderItem... orderItems) {

        Order order = new Order(); // Order 객체 생성
        order.setMember(member);
        order.setDelivery(delivery);
        for (OrderItem orderItem : orderItems)
        {
            order.addOrderItem(orderItem);
        }
        order.setOrderStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());

        return order; // 생성된 order 객체를 반환!

    }
   /* // 위 createOrder() 메서드로만 Order 객체를 만들기 위함.
    protected Order(){

        // createOrder()메서드를 이용하여 order 객체를 생성할 수도 있지만, 아래와 같이 Order 객체를 생성할 수도 잇다.
        // Order order = new Order(); // 객체를 생성하여, set,set,set...으로도 생성 가능!
        // order.setOrderItems();
        // order.setOrderDate();
        //.......
        // 그러나, 어떤 개발자는 createOrder()로 생성을 하고, 어떤 개발자는 위와 같이 생성을 해버리면,
        // 유지/보수 측면에서 매우 어렵다.
        // JPA는 스펙상 생성자에 PUBLIC or PROTECTED를 허용한다.
        // 생성자에 PROTECTED를 걸어서, 밖에서 new Order()을 못 쓰게 해서, 생성 방법을 통일 해야 한다.

        //참고로, 이 부분을 @NoArgsConstructor(access = AccessLevel.PROTECTED)로 대체 가능!
    }*/

    //비지니스 로직(해당 클래스의 필드값이나 연관 관계 엔티티에 대한 로직은 해당 클래스 내에서 정의하는 게 객체 지향적임)
    //주문 취소
    public void cancel(){

        if(delivery.getStatus() == DeliveryStatus.COMP) // 상품이 이미 배송완료라면!!!
        {
            throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
        }

        this.setOrderStatus(OrderStatus.CANCEL);    // 여기에서 JPA의 강점이 들어 난다.
        for(OrderItem orderItem : this.orderItems){ // 만약 JPA를 사용하지 않았다고 해 보자.
                                                    // 그러면,  DB Table에 해당 주문의 tuple을 특정해서, status를 OrderStatus.CANCEL로
            orderItem.cancel();                     //변경하는 [SQL문을 작성해야 한다]
                                                    // 그러나, JPA는 dirty checing을 통해 이러한 [SQL문]을 작성하지 않아도
        }                                           // 자동으로 해당 UPDATE SQL문을 만들어 준다.
    }

    //조회 로직
    //전체 주문 가격 조회
    public int getTotalPrice(){

        int totalPrice = 0;
        for(OrderItem orderItem : this.orderItems){
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
    }
}
