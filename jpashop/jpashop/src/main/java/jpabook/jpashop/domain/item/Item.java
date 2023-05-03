package jpabook.jpashop.domain.item;


import jakarta.persistence.*;
import jpabook.jpashop.domain.Category;
import jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@Getter
@Setter
public abstract class Item {

    @Id@GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    //비즈니즈 로직(해당 데이터에 대한 로직은 그 데이터를 가지고 있는 객체에서 정의하는 것이 좋음)
    //재고 증가
    public void addStock(int stockQuantity){
        this.stockQuantity += stockQuantity;
    }

    //재고 줄이기
    public void removeStock(int stockQuantity){

        if(this.stockQuantity - stockQuantity < 0){
            throw new NotEnoughStockException("need more stock");
        }
        this.stockQuantity = this.stockQuantity - stockQuantity;

    }
}


