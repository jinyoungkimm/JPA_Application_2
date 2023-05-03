package jpabook.jpashop.repository;


import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository { // Item 객체에 대한 Repository이다.

    private final EntityManager entityManager;

    public void save(Item item){

        if(item.getId() == null){ // 해당 Item 객체는 Context에서 가져 온 것이 아니다.
            entityManager.persist(item);
        }
        else{ // 헤당 Item 객체는 Context에서 조회한 것이다.
            entityManager.merge(item);
        }
    }

    public Item findOne(Long id){
        return entityManager.find(Item.class,id);
    }

    public List<Item> findAll(){

        return entityManager.createQuery("select i from Item i",Item.class)
                .getResultList();
    }




}
