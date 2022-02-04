package jpa.book.JPAShop;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity // JPA를 통해 테이블과 매핑하기 위해서 이 어노테이션을 붙여야한다.
@Getter @Setter // Lombok
public class Member {

    @Id // 기본키(Primary Key)가 맵핑됨
    @GeneratedValue // 자동 생성
    private Long id;
    private String username;
}
