package jpa.book.JPAShop;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class MemberRepository {

    @PersistenceContext // EntityManager 자동 주입
    private EntityManager em;

    public Long save(Member member) {
        em.persist(member); // persist : 저장
        return member.getId(); // Long을 반환하는 이유 : SideEffect가 발생할 여지를 최소화
    }

    public Member find(Long id) {
        return em.find(Member.class, id); // find : 조회
    }
}
