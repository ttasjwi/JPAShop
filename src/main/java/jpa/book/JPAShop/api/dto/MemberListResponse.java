package jpa.book.JPAShop.api.dto;

import jpa.book.JPAShop.domain.Member;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberListResponse {

    private final List<MemberListElement> members;

    public static MemberListResponse create(List<Member> memberEntities) {
        List<MemberListElement> members = memberEntities.stream()
                .map(MemberListElement::new)
                .collect(Collectors.toList());

        return new MemberListResponse(members);
    }

    @Data
    static class MemberListElement {

        private Long id;
        private String name;
        private String city;
        private String street;
        private String zipcode;

        public MemberListElement(Member memberEntity) {
            this.id = memberEntity.getId();
            this.name = memberEntity.getName();
            this.city = memberEntity.getAddress().getCity();
            this.street = memberEntity.getAddress().getStreet();
            this.zipcode = memberEntity.getAddress().getZipcode();
        }
    }
}
