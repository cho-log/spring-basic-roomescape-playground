package roomescape.reservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.LoginMember;
import roomescape.member.Member;
import roomescape.member.MemberRepository;
import roomescape.theme.Theme;
import roomescape.theme.ThemeRepository;
import roomescape.time.Time;
import roomescape.time.TimeRepository;
import roomescape.waiting.WaitingRepository;

import java.util.List;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private ReservationRepository reservationRepository;
    private TimeRepository timeRepository;
    private ThemeRepository themeRepository;
    private MemberRepository memberRepository;
    private WaitingRepository waitingRepository;

    public ReservationService(ReservationRepository reservationRepository, TimeRepository timeRepository,
                              ThemeRepository themeRepository, MemberRepository memberRepository, WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.waitingRepository = waitingRepository;
    }

    @Transactional
    public ReservationResponse save(ReservationRequest reservationRequest, LoginMember loginMember) {
        Time time = timeRepository.findByIdAndDeletedFalse(reservationRequest.getTime()).orElseThrow();
        Theme theme = themeRepository.findByIdAndDeletedFalse(reservationRequest.getTheme()).orElseThrow();

        if (reservationRepository.existsByThemeAndDateAndTime(theme, reservationRequest.getDate(), time)) {
            throw new IllegalStateException("이미 예약된 시간입니다.");
        }

        Reservation reservation = reservationRepository.save(
                resolveReservation(reservationRequest, loginMember, time, theme));

        String name = resolveName(reservation);
        return new ReservationResponse(reservation.getId(), name, reservation.getTheme().getName(), reservation.getDate(), reservation.getTime().getValue());
    }

    private Reservation resolveReservation(ReservationRequest reservationRequest, LoginMember loginMember, Time time, Theme theme) {
        String requestName = reservationRequest.getName();
        boolean hasName = requestName != null && !requestName.isBlank();

        if (hasName && !isAdmin(loginMember)) {
            throw new IllegalArgumentException("예약자 이름은 관리자만 지정할 수 있습니다.");
        }
        if (hasName) {
            return new Reservation(requestName, reservationRequest.getDate(), time, theme);
        }

        Member member = memberRepository.findById(loginMember.getId()).orElseThrow();
        return new Reservation(member, reservationRequest.getDate(), time, theme);
    }

    @Transactional
    public void deleteById(Long id) {
        reservationRepository.deleteById(id);
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAllWithDetails().stream()
                .map(it -> new ReservationResponse(it.getId(), resolveName(it), it.getTheme().getName(), it.getDate(), it.getTime().getValue()))
                .toList();
    }

    public List<MyReservationResponse> findMine(LoginMember loginMember) {
        List<MyReservationResponse> reservations = reservationRepository.findByMemberIdWithDetails(loginMember.getId()).stream()
                .map(it -> new MyReservationResponse(it.getId(), it.getTheme().getName(), it.getDate(), it.getTime().getValue(), "예약"))
                .toList();

        List<MyReservationResponse> waitings = waitingRepository.findWaitingsWithRankByMemberId(loginMember.getId()).stream()
                .map(it -> new MyReservationResponse(
                        it.getWaiting().getId(),
                        it.getWaiting().getTheme().getName(),
                        it.getWaiting().getDate(),
                        it.getWaiting().getTime().getValue(),
                        (it.getRank() + 1) + "번째 예약대기"))
                .toList();

        return Stream.concat(reservations.stream(), waitings.stream()).toList();
    }

    private String resolveName(Reservation reservation) {
        if (reservation.getMember() != null) {
            return reservation.getMember().getName();
        }
        return reservation.getName();
    }

    private boolean isAdmin(LoginMember loginMember) {
        return "ADMIN".equals(loginMember.getRole());
    }
}
