package jpabook.jpashop.domain;


import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.Getter;

@Embeddable
@Getter

public class Address { // 값 타입은 변경되며느 안 되므로, Immutable Object로 만들어 줘야 한다.

private String city;
private String street;
private String zipcode;

    // 객체 생성 시점에만 생성자를 통해서 값을 설정할 수 있도록 한다. ( 물론, setter도 없애야 한다 )
    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }

  // protected이므로, 클라이언트 코드에서 new를 통해서는 객체 생성을 못한다.
  // JPA 스펙 상, 기본 생성자는 protected을 권장한다.
  // 그래야, 개발자들이 protected를 보고 " 어, 기본 생성자는 함부로 new를 통해 호출시키면 안되는 것이구나"라는 것을 직관적으로 안다.
  // 오로지, 매개변수 생성자를 통해서만 생성이 가능하구나 라는 것을 알게 된다.
    protected Address() {

    }

}
