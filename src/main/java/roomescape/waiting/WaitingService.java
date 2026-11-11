package roomescape.waiting;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import roomescape.auth.LoginMember;
import roomescape.member.Member;
import roomescape.member.MemberNotFoundException;
import roomescape.member.MemberRepository;
import roomescape.reservation.ReservationAlreadyExistsException;
import roomescape.reservation.ReservationUnavailableException;
import roomescape.reservation.ReservationRepository;
import roomescape.theme.Theme;
import roomescape.theme.ThemeRepository;
import roomescape.time.Time;
import roomescape.time.TimeRepository;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final MemberRepository memberRepository;
    private final TimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public WaitingService(WaitingRepository waitingRepository, MemberRepository memberRepository,
                          TimeRepository timeRepository, ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.waitingRepository = waitingRepository;
        this.memberRepository = memberRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public WaitingResponse save(WaitingRequest waitingRequest, LoginMember loginMember) {
        Member member = memberRepository.findById(loginMember.getId()).orElseThrow(MemberNotFoundException::new);
        String date = waitingRequest.date();
        Time time = timeRepository.findById(waitingRequest.time()).orElseThrow();
        Theme theme = themeRepository.findById(waitingRequest.theme()).orElseThrow();

        validateWaiting(member, date, theme, time);

        Waiting waiting = new Waiting(member, date, time, theme);

        Waiting savedWaiting = waitingRepository.save(waiting);

        long earlierCount = waitingRepository.countEarlierWaitings(savedWaiting.getTheme(), savedWaiting.getDate(),savedWaiting.getTime(), savedWaiting.getId());
        long waitingNumber = earlierCount + 1;

        return new WaitingResponse(savedWaiting.getId(), waitingNumber);
    }

    private void validateWaiting(Member member, String date, Theme theme, Time time) {
        boolean reservationExists = reservationRepository.existsByDateAndThemeAndTime(date, theme, time);
        boolean myReservationExists = reservationRepository.existsByMemberAndDateAndThemeAndTime(member, date, theme, time);
        boolean waitingExists = waitingRepository.existsByMemberAndDateAndThemeAndTime(member, date, theme, time);

        if (!reservationExists) {
            throw new ReservationUnavailableException();
        }

        if (myReservationExists) {
            throw new ReservationAlreadyExistsException();
        }

        if (waitingExists) {
            throw new WaitingAlreadyExistsException();
        }
    }

    @Transactional
    public void deleteById(Long id, LoginMember loginMember) {
        Waiting waiting = waitingRepository.findById(id).orElseThrow();

        if (!waiting.getMember().getId().equals(loginMember.getId())) {
            throw new WaitingAccessDeniedException();
        }

        waitingRepository.delete(waiting);
    }
}
