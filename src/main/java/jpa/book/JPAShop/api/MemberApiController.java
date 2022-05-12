package jpa.book.JPAShop.api;

import jpa.book.JPAShop.api.dto.*;
import jpa.book.JPAShop.domain.Member;
import jpa.book.JPAShop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

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

    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        List<Member> members = memberService.findMembers();
        return members;
    }

    @GetMapping("/api/v2/members")
    public MemberListResponse membersV2() {
        List<Member> findMemberEntities = memberService.findMembers();
        return MemberListResponse.create(findMemberEntities);
    }
}
