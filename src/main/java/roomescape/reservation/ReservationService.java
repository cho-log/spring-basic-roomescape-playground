package roomescape.reservation;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.LoginMember;
import roomescape.member.Member;
import roomescape.member.MemberRepository;
import roomescape.theme.Theme;
import roomescape.theme.ThemeRepository;
import roomescape.time.Time;
import roomescape.time.TimeRepository;
import roomescape.waiting.WaitingRepository;
import roomescape.waiting.WaitingWithRank;

@Service
public class ReservationService {

    private ReservationRepository reservationRepository;
    private MemberRepository memberRepository;
    private TimeRepository timeRepository;
    private ThemeRepository themeRepository;
    private WaitingRepository waitingRepository;

    public ReservationService(ReservationRepository reservationRepository,
        MemberRepository memberRepository, TimeRepository timeRepository,
        ThemeRepository themeRepository, WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.waitingRepository = waitingRepository;
    }

    public ReservationResponse save(ReservationRequest reservationRequest,
        LoginMember loginMember) {
        Member member;
        if (reservationRequest.getName() != null) {
            member = memberRepository.findByName(reservationRequest.getName());
        } else {
            member = memberRepository.findById(loginMember.getId()).orElseThrow();
        }

        Time time = timeRepository.findById(reservationRequest.getTime()).orElseThrow();
        Theme theme = themeRepository.findById(reservationRequest.getTheme()).orElseThrow();

        if (reservationRepository.existsByDateAndTimeAndTheme(
            reservationRequest.getDate(), time, theme)) {
            throw new IllegalArgumentException("이미 존재합니다");
        }

        Reservation unSavedReservation;
        if (reservationRequest.getName() != null) {
            unSavedReservation = new Reservation(reservationRequest.getName(),
                reservationRequest.getDate(), time, theme);
        } else {
            unSavedReservation = new Reservation(member, reservationRequest.getDate(), time, theme);
        }

        Reservation reservation = reservationRepository.save(unSavedReservation);

        String reservationName;
        if (reservation.getName() != null) {
            reservationName = reservation.getName();
        } else {
            reservationName = reservation.getMember().getName();
        }
        return new ReservationResponse(reservation.getId(), reservationName,
            reservation.getTheme().getName(), reservation.getDate(),
            reservation.getTime().getValue());
    }

    public void deleteById(Long id) {
        reservationRepository.deleteById(id);
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll().stream()
            .map(it -> new ReservationResponse(it.getId(), it.getName(), it.getTheme().getName(),
                it.getDate(), it.getTime().getValue()))
            .toList();
    }

    public List<MyReservationResponse> findMine(LoginMember loginMember) {
        List<Reservation> reservations = reservationRepository.findByMemberId(loginMember.getId());
        List<WaitingWithRank> waitings = waitingRepository.findWaitingsWithRankByMemberId(
            loginMember.getId());

        List<MyReservationResponse> myReservationResponses = reservations.stream()
            .map(reservation -> {
                return new MyReservationResponse(reservation.getId(),
                    reservation.getTheme().getName(),
                    reservation.getDate(),
                    reservation.getTime().getTime(),
                    "예약");
            }).toList();

        List<MyReservationResponse> myReservationWaitingResponses = waitings.stream()
            .map(waitingWithRank -> {
                return new MyReservationResponse(waitingWithRank.getWaiting().getId(),
                    waitingWithRank.getWaiting().getTheme().getName(),
                    waitingWithRank.getWaiting().getDate(),
                    waitingWithRank.getWaiting().getTime().getTime(),
                    (waitingWithRank.getRank() + 1) + "번째 예약대기");
            }).toList();

        List<MyReservationResponse> result =
            new ArrayList<>(myReservationResponses);

        result.addAll(myReservationWaitingResponses);

        return result;
    }
}
