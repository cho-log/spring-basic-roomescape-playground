package roomescape.reservation;

import auth.LoginMember;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.AuthorizationException;
import roomescape.member.Member;
import roomescape.member.MemberRepository;
import roomescape.member.Role;
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
    private WaitingRepository waitingRepository;
    private TimeRepository timeRepository;
    private ThemeRepository themeRepository;
    private MemberRepository memberRepository;

    public ReservationService(ReservationRepository reservationRepository, WaitingRepository waitingRepository, TimeRepository timeRepository, ThemeRepository themeRepository, MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public ReservationResponse save(ReservationRequest reservationRequest, LoginMember loginMember) {
        Time time = timeRepository.findByIdAndDeletedFalse(reservationRequest.getTime())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시간입니다."));
        Theme theme = themeRepository.findByIdAndDeletedFalse(reservationRequest.getTheme())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테마입니다."));

        Reservation reservation = reservationRepository.save(
                createReservation(reservationRequest, loginMember, time, theme));

        return new ReservationResponse(reservation.getId(), reservation.getReserverName(), theme.getName(), reservation.getDate().toString(), time.getValue());
    }

    @Transactional
    public void deleteById(Long id) {
        reservationRepository.deleteById(id);
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll().stream()
                .map(it -> new ReservationResponse(it.getId(), it.getReserverName(), it.getTheme().getName(), it.getDate().toString(), it.getTime().getValue()))
                .toList();
    }

    public List<MyReservationResponse> findMine(LoginMember loginMember) {
        Stream<MyReservationResponse> reservations = reservationRepository.findByMemberId(loginMember.id()).stream()
                .map(MyReservationResponse::from);
        Stream<MyReservationResponse> waitings = waitingRepository.findWaitingsWithRankByMemberId(loginMember.id()).stream()
                .map(MyReservationResponse::from);

        return Stream.concat(reservations, waitings).toList();
    }

    private Reservation createReservation(ReservationRequest reservationRequest, LoginMember loginMember, Time time, Theme theme) {
        Member member = memberRepository.findById(loginMember.id())
                .orElseThrow(() -> new AuthorizationException("존재하지 않는 회원입니다."));

        if (member.getRole() == Role.ADMIN && reservationRequest.getName() != null) {
            return Reservation.forGuest(reservationRequest.getName(), reservationRequest.getDate(), time, theme);
        }
        return Reservation.forMember(member, reservationRequest.getDate(), time, theme);
    }
}
