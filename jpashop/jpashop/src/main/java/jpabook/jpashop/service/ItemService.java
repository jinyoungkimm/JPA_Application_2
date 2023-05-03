package jpabook.jpashop.service;


import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // readOnly = true는 성능 최적화를 해준다.
public class ItemService { // Item 객체에 대한 Service

    private final ItemRepository itemRepository;

    @Transactional // readOnly == false
    public void saveItem(Item item){

        itemRepository.save(item);

    }

    /**
     * 영속성 컨텍스트가 자동 변경
     */
    @Transactional
    public void updateItem(Long id, String name, int price, int stockQuantity)
    {
        Item item = itemRepository.findOne(id); // [영속] 엔티티
        item.setName(name);
        item.setPrice(price);
        item.setStockQuantity(stockQuantity);

        //item은 Context에 관리되므로, dirthChecking에 의해 [수정]이 일어난다.

    }


    public List<Item> findItems(){
        return itemRepository.findAll();
    }

    public Item findOne(Long id){
        return itemRepository.findOne(id);
    }
}
