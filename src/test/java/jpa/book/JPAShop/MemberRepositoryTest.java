package jpa.book.JPAShop;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class) // JUnit5에게 Spring 관련 테스트 하는 것을 알려줘야함.
@SpringBootTest // 스프링 부트 테스트
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    
    @Test
    @DisplayName("멤버등록 - 정합성 테스트")
    @Transactional // 트랜잭션
    @Rollback(false) // 롤백되지 않음
    public void memberTest() {
        //given
        Member member = new Member();
        member.setUsername("memberA");

        //when
        Long saveId = memberRepository.save(member);
        Member findMember = memberRepository.find(saveId);

        //then
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(member).isSameAs(findMember); // 엔티티 동일성 보장
        softAssertions.assertThat(member.getId()).isEqualTo(findMember.getId()); // id 같다
        softAssertions.assertThat(member.getUsername()).isEqualTo(findMember.getUsername()); // username 같다
        softAssertions.assertAll();
    }

}