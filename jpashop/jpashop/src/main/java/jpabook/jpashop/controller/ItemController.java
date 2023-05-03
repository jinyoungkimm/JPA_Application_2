package jpabook.jpashop.controller;


import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/items/new") // [상품 등록] - 1
    public String createForm(Model model){

        model.addAttribute("form",new BookForm());
        return "items/createItemForm";
    }

    @PostMapping("/items/new") // [상품 등록] - 2
    public String create(BookForm form){

        // 이 부분, Book : createBook(..)으로 편의 생성 메서드로 리팩토링 하자!!! setter가 많으면 안 좋음.
        Book book = new Book();
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());

        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());

        itemService.saveItem(book);

        return "redirect:/"; // 정상적으로 저장되면, 홈 화면으로 돌아간다.

    }

    @GetMapping(value = "/items") // [상품 목록]
    public String list(Model model) {
        List<Item> items = itemService.findItems();
        model.addAttribute("items", items);
        return "items/itemList";
    }


    @PostMapping(value = "/items/{itemId}/edit")  // [상품 목록] -> [수정] -> [수정] 페이지로부터 데이터를 받아서 업데이트!
    public String updateItem(@PathVariable Long itemId,@ModelAttribute("form") BookForm form) {

       /* Book book = new Book();
        book.setId(form.getId()); // 기존 [식별자]를 가지고 있으므로, 임의로 만든 book이지만 [준영속] 상태의 엔티티로 볼 수 있다(ppt 89)
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());

        itemService.saveItem(book); // [수정](saveItem(book)으로 [수정]을 하려하면, [병합] 전략으로 수정하도록 코드를 짜놓음)*/

        itemService.updateItem(itemId, form.getName(),form.getPrice(),form.getStockQuantity()); // dirty checking으로 [수정]됨.

        return "redirect:/items"; // [수정]이 완료되면, redirect
    }

    @GetMapping("items/{itemId}/edit") // [상품 목록] -> [수정] 클릭 시, [수정] 페이지로 이동!
    public String updateItemForm(@PathVariable("itemId") Long id, Model model){

        Book book = (Book)itemService.findOne(id);

        //Book 클래스에다가 static으로 편의 생성 메서드(createBook(...))를 만드는 것이 훨씬 좋음. setter 일일이 언제 치냐고... ㅠㅠ
        BookForm form = new BookForm();
        form.setId(book.getId());
        form.setName(book.getName());
        form.setPrice(book.getPrice());
        form.setStockQuantity(book.getStockQuantity());
        form.setAuthor(book.getAuthor());
        form.setIsbn(book.getIsbn());

        model.addAttribute("form",form);

        return "items/updateItemForm";
    }


}
