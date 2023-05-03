package jpabook.jpashop.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Member {

    @Id@GeneratedValue
    @Column(name = "member_id") // 이걸 안 해주면, MEMBER의 PK명이 "id"로 된다.
    private Long id;

    @NotEmpty
    private String name;

    @Embedded //생략 가능
    private Address address; // 임베디드

    @JsonIgnore // API 호출 시, JSON으로 나갈 때, orders 속성값이 도메인 엔티티에 들어 있다는 걸 숨기기 위해, Json에 "orders"를 넣지 않고 나머지 필드명만 반환
    @OneToMany(mappedBy = "member") //Member로는 연관 관계의 조회만 가능! 연관 관계의 변경 불가능! orders에 뭔가 값을 넣는 다고 해서, 연관 관계가 바뀌지는 않음.
    private List<Order> orders = new ArrayList<>();


}
