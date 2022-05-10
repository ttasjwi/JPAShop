package jpa.book.JPAShop.service;

import jpa.book.JPAShop.domain.Member;
import jpa.book.JPAShop.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("회원가입 후 같은 아이디로 조회했을 때 같은 회원이 반환되어야한다.")
    public void joinTest() {
        //given
        Member member = new Member();
        member.setName("ttasjwi");

        //when
        Long joinId = memberService.join(member);

        //then
        assertThat(memberRepository.findOne(joinId)).isSameAs(member);
    }

    @Test
    @DisplayName("중복된 이름으로 회원가입 시도 시 예외가 발생해야한다.")
    public void joinFailureTest() {
        //given
        Member member1 = new Member();
        member1.setName("ttasjwi");

        Member member2 = new Member();
        member2.setName("ttasjwi");

        //when & then
        memberService.join(member1);
        assertThatThrownBy(() -> memberService.join(member2))
                .isInstanceOf(IllegalStateException.class);

    }
}