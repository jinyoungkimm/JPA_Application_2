package jpabook.jpashop.service;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;


@RunWith(SpringRunner.class)
@SpringBootTest // 스프링 부트를 띄우고 테스트를 하겠다. 이게 없으면, @Autowired가 제대로 동작x.
@Transactional
class MemberServiceTest {


    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager em;

    // 이런 의문이 들 수가 있다. memberService 안에 이미 memberRepository가 주입이 돼 있을텐데,
    // 왜 또 여기서 memberRepository를 주입 받아야 하는지!!1
    // memberRepository는 [싱글톤] Bean이다. 고로, memberService 안의 memberRepository bean과
    // @Autowired로 주입받은 memberRepository는 같은 객체이다.
    // 근데, memberService 객체에서 memberRepository 객체를 꺼내서 사용하는 것이 불편하므로 여기서는 따로 빼서
    // 사용하고 있다.

    @Test
    public void 회원가입() throws Exception{

        //givien
        Member member = new Member();
        member.setName("kim");

        //when
        Long saveId = memberService.join(member);

        //then
        // em.flush(); // insert 쿼리를 보고 싶을 때!
        assertEquals(member,memberRepository.findOne(saveId)); // JPA는 같은 트랜잭션 내에서는
        // 같은 id로 조회한 객체는 단 1개로 보장이 된다. 같은 id로 객체가 2,3개씩 절대 만들어 지지 않는다.
    }

    //@Test(expected = IllegalStateException.class)
    @Test
    public void 중복_회원_예외() throws Exception{

        //givien
        Member member1 = new Member();
        member1.setName("kim1");

        Member member2 = new Member();
        member2.setName("kim1");

        //when
        memberService.join(member1);
        try {
            memberService.join(member2); // 여기서 예외가 올라 와야 한다. 그리고는 더 이상 코드의 흐름이 아래로 내려 가지 않고
            // 이 메서드의 실행이 중단되어야 한다.
        }catch (IllegalStateException e){
            return ;
        }
        //then
        fail("코드의 실행이 여기까지 오는 것은 비정상입니다.");
    }


}