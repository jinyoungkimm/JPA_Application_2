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

    @JsonIgnore // API 호출 시, 양방향 설정으로 인한 JSON 무한 루프 에러를 막기 위해서 @JsonIgore을 해줘야 한다. Member :: orders에 해줘도 되고, Order :: member에 해줘도 된다.
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();


}
