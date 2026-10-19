package roomescape.waiting;

import org.springframework.stereotype.Service;
import roomescape.member.LoginMember;
import roomescape.member.Member;
import roomescape.member.MemberRepository;
import roomescape.theme.Theme;
import roomescape.theme.ThemeRepository;
import roomescape.time.Time;
import roomescape.time.TimeRepository;

@Service
public class WaitingService {

    private WaitingRepository waitingRepository;
    private MemberRepository memberRepository;
    private TimeRepository timeRepository;
    private ThemeRepository themeRepository;

    public WaitingService(WaitingRepository waitingRepository, MemberRepository memberRepository,
        TimeRepository timeRepository, ThemeRepository themeRepository) {
        this.waitingRepository = waitingRepository;
        this.memberRepository = memberRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
    }

    public Waiting save(WaitingRequest waitingRequest, LoginMember loginMember) {
        Member member = memberRepository.findByEmail(loginMember.getEmail());
        String date = waitingRequest.getDate();
        Time time = timeRepository.findById(waitingRequest.getTime()).orElseThrow();
        Theme theme = themeRepository.findById(waitingRequest.getTheme()).orElseThrow();

        if (waitingRepository.existsByMemberAndDateAndTimeAndTheme(member, date, time, theme)) {
            throw new IllegalArgumentException("이미 존재합니다");
        }
        Waiting waiting = new Waiting(member, date, time, theme);

        return waitingRepository.save(waiting);
    }

    public void deleteById(Long id) {
        waitingRepository.deleteById(id);
    }
}
