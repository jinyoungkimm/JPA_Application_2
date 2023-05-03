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

import java.util.List;
import java.util.stream.Collectors;


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


    @GetMapping("/api/v1/members")
    public List<Member> membersV1(){ //[회원 조회]용 Rest API - 1

        return memberService.findMembers(); // [도메인 엔티티]를 반환! ( memberV2()에서 개선할 예정 )
                                            // 그리고 컬렉션(List<Member>)를 [직접] 반환하고 있다.
    }

    @GetMapping("api/v2/members")
    public Result membersV2(){ //[회원 조회]용 Rest API - 2

        List<Member> findmembers = memberService.findMembers();

        //1] findMembers 컬렉션을 Rest API 스펙에 맞게 [MemberDTO 컬렉션]으로 변환
        List<MemberDTO> memberDTO = findmembers.stream()
                .map(m -> new MemberDTO(m.getName()))
                .collect(Collectors.toList());
        // 2] [MemberDTO 컬렉션]을 Result 클래스의 필드값으로 넘겨서 반환함으로서, 컬렉션을 [직접] 반환하는 것이 아닌,
        // Result 객체의 필드값으로[간접적]으로 컬렉션을 넘김.
        // 즉, 컬렉션을 한 번 Wrapping을 해서, 넘겨야 한다.
        return new Result(memberDTO);

    }

    @Data
    @AllArgsConstructor
    public static class Result<T>{

        private T data; // Rest API를 호출한 개발자의 화면에는 "data" : "[회원 목록1,2,3,,,,,n]" 형태로 보여질 것이다.
                    // https://jbluke.tistory.com/411 사이트를 참조하면 보여지는 화면의 차이점인지 파악 가능할 것이다.
    }

    @Data
    @AllArgsConstructor
    public static class MemberDTO{ // Rest API의 스펙!

        private String name; // Member의 name만을 반환!

    }

}
