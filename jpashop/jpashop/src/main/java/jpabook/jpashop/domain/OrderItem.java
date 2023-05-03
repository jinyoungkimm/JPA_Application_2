package jpabook.jpashop.domain;


import jakarta.persistence.*;
import jpabook.jpashop.domain.item.Item;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.aspectj.weaver.ast.Or;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem { // 주문한 상품에 대한 클래스(상품을 주문했을 때, 파생되는 데이터들을 정의)

    @Id@GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice; // 주문 가격
     private int count; // 주문 수량

    //orderItem 객체 생성 메서드 : orderItem처럼 연관 관계 객체가 많고, 필드 값이 많은 복잡한 객체를 만들 때에는
    // 아래와 같이 생성 편의 메서드를 만들어 놓으면 좋다. 왜냐하면, 만약 orderItem 객체에 뭔가 문제가 생겨서 살펴 보고 싶을 때
    // createOrderItem(..) 부분만을 보면 되기 때문!
    public static OrderItem createOrderItem(Item item,int orderPrice,int count){
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);

        item.removeStock(count); // 주문이 들어오면 기본적으로 해당 상품의 재고가 줄어 들어야 한다.

        return orderItem;
    }

    //비지니스 로직
    public void cancel() {

        getItem().addStock(count); // 상품(Item)의 재고량(stockQuantity)이 주문 [취소]로 인해, 늘어 나야 한다.
    }

    public int getTotalPrice(){
        return orderPrice * count;
    }

}
