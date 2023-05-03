package jpabook.jpashop.api;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@RestController // @Controller + @ResponseBody
@RequiredArgsConstructor
public class MemberApiController { // Rest API용 Controller이다(여기서는 JSON을 사용한다고 가정)

    private final MemberService memberService;


    @PostMapping("/api/v1/members") // JSON으로 온 HTTP 메시지를 Member 객체로 [자동] 매핑.
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member){ //[회원 등록] API(V1)

        Long id = memberService.join(member);
        return new CreateMemberResponse(id); // 반대로, CreateMemberResponse 객체를 [자동]으로 JSON으로 매핑하여 반환.
    }

    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request){//[회원 등록] API(V2)

        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);

        return new CreateMemberResponse(id);


    }

    @Data
    static class CreateMemberRequest{ // 매개 변수용 DTO && saveMemberV2 API의 스펙 문서

    private String name;


    }
     @Data
     static class CreateMemberResponse{ //반환용 DTO && saveMemberV1 API의 스펙 문서

        private Long id;

         public CreateMemberResponse(Long id) {
             this.id = id;
         }
     }


     @PutMapping("/api/v2/members/{id}") // [회원 수정] Rest API
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id, @RequestBody @Valid UpdateMemberRequest request){


         memberService.update(id,request.getName()); // [수정] 전략은 무조건 [dirty checking] 전략 사용
                                                    // 매개변수 id는 [기존 식별자]이다.
                                                    // 고로, 함부로 임의의 객체를 만들어서, 그 객체에 [기존 식별자]를 가지게 하여
                                                    // [준영속] 엔티티로 만들어 버려서, [dirth checking] 사용이 불가능하도록 만들면 안된다.
         Member findMember = memberService.findOne(id);
         return new UpdateMemberResponse(findMember.getId(), findMember.getName());
     }

    @Data
    public static class UpdateMemberRequest{ // 매개 변수용 DTO && updateMemberV2 API의 스펙 문서

        @NotEmpty
        private String name;


    }

    @Data
    @AllArgsConstructor // 모든 [매개 변수의 경우의 수]에 대한 [모든] 생성자를 자동 생성.
    public static class UpdateMemberResponse{ //반환용 DTO && updateMemberV1 API의 스펙 문서

        @NotEmpty
        private Long id;
        private String name;

    }


}
