package jpabook.jpashop.controller;


import jakarta.validation.Valid;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController { //  [회원 가입],[회원 목록] 페이지를 위한 controller.
    //1] 회원 가입 페이지를 보여 주고 회원 가입 정보를 입력 받음 : GET createForm(Model model)
    //2] post 방식으로 회원 가입 정보가 오고, 그것을 DB에 저장. : POST create(MemberForm form)
    //3] [회원 목록] 페이지를 보여줌.
    private final MemberService memberService;

    @GetMapping("/members/new") // [회원 가입] 페이지 - 1
    private String createForm(Model model){
        model.addAttribute("memberForm",new MemberForm()); // html 페이지에 빈 MemberForm 객체를 들고 가는 이유 : @NotEmpty() 등의
        return "members/createMemberForm";                              // validation 기능을 html 페이지에서 사용하기 위해!
    }

    @PostMapping("/members/new") // [회원 가입] 페이지 - 2
    public String create(@Valid MemberForm form, BindingResult result){ // @Valid가 있으면, Spring이 MemberForm 클래스 안의 @NotEmpty 등을 보고, 검증(validation)을 해줌.
            // @Valid를 통해 에러가 있으면, 아래의 코드는 실행이 되지 않고, 에러 페이지를 내 뱉는다. 그러나 BindingResult 객체가 있으면, 그 에러 정보등을
        // 가지고, 아래의 코드를 그대로 실행을 한다.

        if(result.hasErrors()){
            return "members/createMemberForm"; // Spring이 BindingResult 객체를 해당 html로 끌고 와서, 저장된 에러 정보를 사용할 수 있도록 도와준다.
                                               //해당 Form에 들어 가보면, th:error=*{name}이라는 부분이 있다. 이 부분에 에러 정보를 뿌려준다.
        }                                      // 타임리프와 Spring이 잘 통합이 돼 있기에 가능한 기능!
                                                // 이건, org.thymeleaf.thymeleaf-spring 라이브러리가 제공하는 기능!(외부 라이브러리에서 검색 가능)
        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());
        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);

        memberService.join(member);
        return "redirect:/"; // 저장이 끝나고 나면, 리다이렉트를 해서 다시 home 화면(localhost:8080/) 페이지로 돌아 가게 한다.
    }

    @GetMapping("/members") // [회원 목록] 페이지!
    public String list(Model model){
        List<Member> members = memberService.findMembers();
        model.addAttribute("members",members);
        return "members/memberList";
    }


}
