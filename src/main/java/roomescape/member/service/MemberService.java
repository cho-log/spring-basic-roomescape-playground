package roomescape.member.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import roomescape.exception.member.DuplicateMemberException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.dto.MemberRequest;
import roomescape.member.dto.MemberResponse;
import roomescape.member.repository.MemberRepository;

@Service
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public MemberResponse createMember(MemberRequest memberRequest) {
        try {
            Member member = memberRepository.save(
                    new Member(
                            memberRequest.getName(),
                            memberRequest.getEmail(),
                            memberRequest.getPassword(),
                            Role.USER
                    )
            );

            return new MemberResponse(
                    member.getId(),
                    member.getName(),
                    member.getEmail()
            );
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateMemberException("이미 가입된 이메일입니다.");
        }
    }
}
