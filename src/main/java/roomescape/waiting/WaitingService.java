package roomescape.waiting;

import auth.LoginMember;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.AuthorizationException;
import roomescape.member.Member;
import roomescape.member.MemberRepository;
import roomescape.reservation.ReservationRepository;
import roomescape.theme.Theme;
import roomescape.theme.ThemeRepository;
import roomescape.time.Time;
import roomescape.time.TimeRepository;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class WaitingService {
    private WaitingRepository waitingRepository;
    private ReservationRepository reservationRepository;
    private TimeRepository timeRepository;
    private ThemeRepository themeRepository;
    private MemberRepository memberRepository;

    public WaitingService(WaitingRepository waitingRepository, ReservationRepository reservationRepository, TimeRepository timeRepository, ThemeRepository themeRepository, MemberRepository memberRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public WaitingResponse save(WaitingRequest waitingRequest, LoginMember loginMember) {
        Time time = timeRepository.findByIdAndDeletedFalse(waitingRequest.time())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시간입니다."));
        Theme theme = themeRepository.findByIdAndDeletedFalse(waitingRequest.theme())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테마입니다."));
        Member member = memberRepository.findById(loginMember.id())
                .orElseThrow(() -> new AuthorizationException("존재하지 않는 회원입니다."));

        validateNotDuplicated(member, waitingRequest.date(), time, theme);

        Waiting waiting = waitingRepository.save(new Waiting(member, waitingRequest.date(), time, theme));
        Long waitingNumber = waitingRepository.countByDateAndTimeAndThemeAndIdLessThan(
                waiting.getDate(), time, theme, waiting.getId()) + 1;

        return new WaitingResponse(waiting.getId(), waitingNumber);
    }

    @Transactional
    public void deleteById(Long id, LoginMember loginMember) {
        Waiting waiting = waitingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약 대기입니다."));
        if (!waiting.getMember().getId().equals(loginMember.id())) {
            throw new AuthorizationException("본인의 예약 대기만 취소할 수 있습니다.");
        }
        waitingRepository.delete(waiting);
    }

    private void validateNotDuplicated(Member member, LocalDate date, Time time, Theme theme) {
        if (reservationRepository.existsByMemberAndDateAndTimeAndTheme(member, date, time, theme)) {
            throw new DuplicatedReservationException("이미 예약한 시간에는 대기할 수 없습니다.");
        }
        if (waitingRepository.existsByMemberAndDateAndTimeAndTheme(member, date, time, theme)) {
            throw new DuplicatedReservationException("이미 대기 중인 시간입니다.");
        }
    }
}
