package jpa.book.JPAShop.api;

import jpa.book.JPAShop.domain.Address;
import jpa.book.JPAShop.domain.Member;
import jpa.book.JPAShop.service.MemberService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long memberId = memberService.join(member);
        return new CreateMemberResponse(memberId);
    }

    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Member member = request.toEntity();
        Long memberId = memberService.join(member);
        return new CreateMemberResponse(memberId);
    }

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(
            @PathVariable Long id,
            @RequestBody @Valid UpdateMemberRequest request) {

        memberService.update(id, request.getName(), request.getAddress());

        Member updatedMember = memberService.findOne(id);
        return new UpdateMemberResponse(updatedMember);
    }

    @Data
    static class CreateMemberRequest {
        @NotEmpty
        private String name;
        private String city;
        private String street;
        private String zipcode;

        public Member toEntity() {
            Member member = new Member();
            member.setName(name);
            member.setAddress(new Address(city,street, zipcode));
            return member;
        }
    }

    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }

    @Data
    static class UpdateMemberRequest {
        @NotEmpty
        private String name;
        private String city;
        private String street;
        private String zipcode;

        public Address getAddress() {
            return new Address(city, street, zipcode);
        }
    }

    @Data
    static class UpdateMemberResponse {

        private Long id;
        private String name;
        private String city;
        private String street;
        private String zipcode;

        public UpdateMemberResponse(Member updatedMember) {
            this.id = updatedMember.getId();
            this.name = updatedMember.getName();
            this.city = updatedMember.getAddress().getCity();
            this.street = updatedMember.getAddress().getStreet();
            this.zipcode = updatedMember.getAddress().getZipcode();
        }
    }

}
