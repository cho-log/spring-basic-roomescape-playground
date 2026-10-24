package roomescape.reservation;

import org.springframework.stereotype.Service;
import roomescape.auth.LoginMember;
import roomescape.member.Member;
import roomescape.member.MemberNotFoundException;
import roomescape.member.MemberRepository;
import roomescape.theme.Theme;
import roomescape.theme.ThemeRepository;
import roomescape.time.Time;
import roomescape.time.TimeRepository;
import roomescape.waiting.Waiting;
import roomescape.waiting.WaitingRepository;
import roomescape.waiting.WaitingWithRank;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final TimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(ReservationRepository reservationRepository, MemberRepository memberRepository,
                              TimeRepository timeRepository, ThemeRepository themeRepository, WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.waitingRepository = waitingRepository;
    }

    public ReservationResponse save(ReservationRequest reservationRequest, LoginMember loginMember) {
        Time time = timeRepository.findById(reservationRequest.getTime()).orElseThrow();
        Theme theme = themeRepository.findById(reservationRequest.getTheme()).orElseThrow();

        Reservation reservation = createReservation(reservationRequest, loginMember, time, theme);

        Reservation savedReservation = reservationRepository.save(reservation);

        return new ReservationResponse(savedReservation.getId(), savedReservation.getReservationName(), savedReservation.getTheme().getName(), savedReservation.getDate(), savedReservation.getTime().getValue());
    }

    private Reservation createReservation(ReservationRequest reservationRequest, LoginMember loginMember,
                                          Time time, Theme theme) {
        if (reservationRequest.getName() == null) {
            Member member = memberRepository.findById(loginMember.getId()).orElseThrow(MemberNotFoundException::new);

            return new Reservation(
                    member,
                    "",
                    reservationRequest.getDate(),
                    time,
                    theme
            );
        }

        return new Reservation(
                null,
                reservationRequest.getName(),
                reservationRequest.getDate(),
                time,
                theme
        );
    }

    public void deleteById(Long id) {
        reservationRepository.deleteById(id);
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll().stream()
                .map(it -> new ReservationResponse(it.getId(), it.getReservationName(), it.getTheme().getName(), it.getDate(), it.getTime().getValue()))
                .toList();
    }

    public List<MyReservationResponse> findMyReservations(LoginMember loginMember) {
        List<Reservation> reservations = reservationRepository.findByMemberId(loginMember.getId());

        List<WaitingWithRank> waitingsWithRank = waitingRepository.findWaitingsWithRankByMemberId(loginMember.getId());

        List<MyReservationResponse> reservationResponses = reservations.stream()
                .map(it -> new MyReservationResponse(it.getId(), it.getTheme().getName(), it.getDate(), it.getTime().getValue(), "예약"))
                .toList();

        List<MyReservationResponse> waitingResponses = waitingsWithRank.stream()
                .map(it -> {
                    Waiting waiting = it.getWaiting();
                    Long rank = it.getRank();

                    return new MyReservationResponse(
                            waiting.getId(),
                            waiting.getTheme().getName(),
                            waiting.getDate(),
                            waiting.getTime().getValue(),
                            (rank + 1) + "번째 예약대기"
                    );
                })
                .toList();

        List<MyReservationResponse> result = new ArrayList<>(reservationResponses);
        result.addAll(waitingResponses);

        return result;
    }
}
