package jpabook.jpashop.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jpabook.jpashop.domain.item.Item;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.aspectj.weaver.ast.Or;
import org.hibernate.annotations.BatchSize;

@Entity
@Getter
@Setter
@BatchSize(size =  100) // @~ToOne 관계는 필드 내에서 @BatchSize를 사용 못함.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem { // 주문한 상품에 대한 클래스(상품을 주문했을 때, 파생되는 데이터들을 정의)

    @Id@GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @JsonIgnore // 양방향 설정에 따른, JSON의 무한 루프 에러를 막기 위해서 붙여 줘야 함.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice; // 주문 가격
     private int count; // 주문 수량


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
