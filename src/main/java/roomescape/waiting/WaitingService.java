package roomescape.waiting;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import roomescape.member.LoginMember;
import roomescape.member.Member;
import roomescape.member.MemberRepository;
import roomescape.reservation.ReservationRepository;
import roomescape.theme.Theme;
import roomescape.theme.ThemeRepository;
import roomescape.time.Time;
import roomescape.time.TimeRepository;

@Service
@Transactional(readOnly = true)
public class WaitingService {
    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final TimeRepository timeRepository;
    private final ThemeRepository themeRepository;

    public WaitingService(WaitingRepository waitingRepository, ReservationRepository reservationRepository,
                          MemberRepository memberRepository, TimeRepository timeRepository, ThemeRepository themeRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional
    public WaitingResponse save(WaitingRequest request, LoginMember loginMember) {
        if (!reservationRepository.existsByDateAndTimeIdAndThemeId(request.getDate(), request.getTime(), request.getTheme())) {
            throw new IllegalArgumentException("예약이 없는 시간에는 대기할 수 없습니다.");
        }
        if (reservationRepository.existsByMemberIdAndDateAndTimeIdAndThemeId(
                loginMember.getId(), request.getDate(), request.getTime(), request.getTheme())) {
            throw new IllegalArgumentException("본인 예약에는 대기할 수 없습니다.");
        }
        if (waitingRepository.existsByMemberIdAndDateAndTimeIdAndThemeId(
                loginMember.getId(), request.getDate(), request.getTime(), request.getTheme())) {
            throw new IllegalArgumentException("이미 예약 대기 중입니다.");
        }

        Member member = memberRepository.findById(loginMember.getId())
                .orElseThrow(() -> new IllegalArgumentException("대기할 회원이 없습니다."));
        Time time = timeRepository.findById(request.getTime())
                .orElseThrow(() -> new IllegalArgumentException("예약 시간이 없습니다."));
        Theme theme = themeRepository.findById(request.getTheme())
                .orElseThrow(() -> new IllegalArgumentException("테마가 없습니다."));

        Waiting waiting = waitingRepository.save(new Waiting(member, request.getDate(), time, theme));
        long waitingNumber = waitingRepository.countByDateAndTimeIdAndThemeIdAndIdLessThan(
                request.getDate(), request.getTime(), request.getTheme(), waiting.getId()) + 1;
        return new WaitingResponse(waiting.getId(), waitingNumber);
    }

    @Transactional
    public void cancel(Long id, LoginMember loginMember) {
        Waiting waiting = waitingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!waiting.getMember().getId().equals(loginMember.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        waitingRepository.delete(waiting);
    }
}
