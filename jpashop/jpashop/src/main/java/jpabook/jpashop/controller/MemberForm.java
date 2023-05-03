package jpabook.jpashop.controller;


import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

//회원가입 페이지
@Getter@Setter
public class MemberForm {

    @NotEmpty(message = "회원 이름은 필수입니다") // validation 라이브러리 추가해야 함!
    private String name;

    private String city;
    private String street;
    private String zipcode;


}
