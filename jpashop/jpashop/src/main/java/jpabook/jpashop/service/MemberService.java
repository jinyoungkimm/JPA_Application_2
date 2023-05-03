package jpabook.jpashop.service;


import jakarta.persistence.OneToMany;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true) // public으로 지정된 메서드에 붙는다.
                                // 읽기 전용으로 트랜잭션을 걸면, 성능 최적화가 일어난다.
@RequiredArgsConstructor
public class MemberService { // Member 객체에 대한 Service

/*
    @Autowired // 의존 관계 주입 방법 1 : 필드 초기화, 단점 : 한 번 의존관계가 주입되면 의존관계 변경 불가능! 테스트할 때에 이리저리 바꿔 보지 못함.
    MemberRepository memberRepository;

    @Autowired // 의존 관계 주입 방법 2 : setter Injection, 단점 : 런타임에 이 세터에 의해 의존관계가 변경될 수가 있다!
    public void setMemberRepository(MemberRepository memberRepository){
        this.memberRepository = memberRepository;
    }

    @Autowired // 의존 관계 주입 방법 3 :생성자 주입(많이 사용됨). 위 2개의 단점을 모두 보완.
    public MemberService(MemberRepository memberRepository){
        this.memberRepository = memberRepository;
    }
*/

    //의존 관계 주입 방법 4 : final 과 생성자의 조합
    // final 키워드를 붙임으로써 생성자가 없으면, 빨간불이 뜨면서 컴파일 타임에서 오류를 잡아 준다.
    // 또한 final 키워드로 인해 객체 생성 시점에만 값을 바꿀 수가 있고, 그 이후 시점에서는 의존관계가 변경 안됨.
   /*
        private final MemberRepository memberRepository;
        //생성자가 1개 일 때에는 자동으로 @Autowired를 붙여준다.
        public MemberService(MemberRepository memberRepository){

            this.memberRepository = memberRepository;

           }
    */

    //의존 관계 주입 방법 5 : Lombok의 [@AllArgsConstor]과 final의 조합
    //  private final MemberRepository memberRepository 만 있으면 되고
    // 생성자는 Lombok이 만들어 줌.

    //의존 관계 주입 방법 6 : Lombok의 [@RequiredArgsConstructor]과 final의 조합(이 방법이 가장 많이 사용됨)
    private final MemberRepository memberRepository;
    //  private final MemberRepository memberRepository 만 있으면 되고
    // 생성자는 Lombok이 만들어 줌.
    // [@RequiredArgsConstructor]은 final이 붙은 필드에 대해서만 생성자를 만들어 준다.

    //회원 가입
    @Transactional
    public Long join(Member member){

        validateDuplicateMember(member); // 중복 회원 여부 검사
        memberRepository.save(member);

        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        // 이건,사실 옳바르지 못한 중복 검사이다.
        // 만약 2개의 멀티 쓰레드에서 [동시에] [MemberA]라는 이름으로 join을 하려고 한다고 하자.
        // 그래서 이 메서드를 [동시에] 수행을 하면, 해당 시점에는 MemberA라는 이름의 Member가 저장돼 있지 않으므로,
        // 2개의 쓰레드가 동시에 회원가입에 성공을 할 수 있기 때문이다.
        // 위와 같이 2개의 쓰레드가 동시에 회원가입에 성공할 수 없게, DB에 UNIQUE 제약조건을 거는 방법으로
        // 중복 검사를 헤주는 것이 확실하다.
        List<Member> findMemberfs = memberRepository.findByName(member.getName());
        if(!findMemberfs.isEmpty())
        {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    //회원 전체 조회
    //@Transactional(readOnly = true)
     public List<Member> findMembers(){
        return memberRepository.findAll();
     }

     public Member findOne(Long id){

        return memberRepository.findOne(id);
     }


     @Transactional(readOnly = false)
    public void update(Long id, String name) { // [회원 수정] Rest API : [수정] 전략은 무조건 [Dirty Checking]으로!

        Member findMember = memberRepository.findOne(id); // [기존의 식별자]인 id를 통하여, DB or Context에서 조회!
                                                          // findMember는 [영속] 엔티티
        //[영속] 엔티티이므로, set을 사용해서 [Dirth checking] 전략으로 [수정]
        findMember.setName(name);

    }
}
