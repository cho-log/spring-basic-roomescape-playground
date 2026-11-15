package roomescape.domain.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.entity.Member;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.global.exception.ConflictException;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Member createMember(String nickname, String email, String password) {

        if (memberRepository.existsByEmail(email)) {
            throw new ConflictException("이미 가입된 정보입니다.");
        }

        if (memberRepository.existsByNickname(nickname)) {
            throw new ConflictException("이미 사용 중인 닉네임입니다.");
        }

        return memberRepository.save(new Member(nickname, email, password, "USER"));
    }
}
