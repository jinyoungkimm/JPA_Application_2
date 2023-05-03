package jpabook.jpashop.repository;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepository { // Member 객체에 대한 Repository

                // Spring-boot-starter가 EntityManger를 자동으로 주입을 주며(EntityManagerFactory같은 것도 자동으로 만들어줌),
                        // build.gradle에서 [implementation 'org.springframework.boot:spring-boot-starter-data-jpa] 등록을 했으니깐 가능.
                // application.yml에서 등록한 Connectino 정보를 가지고 [자동]으로 연결!!
    private final EntityManager entityManager;

    public void save(Member member){
        entityManager.persist(member);
    }

    public Member find(Long id){
        return entityManager.find(Member.class,id);
    }

    public List<Member> findAll(){

        List<Member> findMembers = entityManager.createQuery("select m from Member m", Member.class)
                .getResultList();

        return findMembers;

    }

    //회원 단일 조회
    public Member findOne(Long id){
        return entityManager.find(Member.class,id);
    }

    public List<Member> findByName(String name){

        List<Member> findMember = entityManager.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
        return findMember;
    }



}
