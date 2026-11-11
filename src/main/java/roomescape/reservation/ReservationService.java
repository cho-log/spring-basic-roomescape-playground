package roomescape.reservation;

import java.util.List;
import java.util.ArrayList;
import roomescape.waiting.Waiting;
import roomescape.waiting.WaitingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.LoginMember;
import roomescape.member.Member;
import roomescape.member.MemberRepository;
import roomescape.theme.Theme;
import roomescape.theme.ThemeRepository;
import roomescape.time.Time;
import roomescape.time.TimeRepository;

@Service
@Transactional(readOnly = true)
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

    @Transactional
    public ReservationResponse save(ReservationRequest request, LoginMember loginMember) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(
                request.getDate(), request.getTime(), request.getTheme())) {
            throw new IllegalArgumentException("이미 예약된 시간입니다.");
        }
        Time time = timeRepository.findById(request.getTime())
                .orElseThrow(() -> new IllegalArgumentException("예약 시간이 없습니다."));
        Theme theme = themeRepository.findById(request.getTheme())
                .orElseThrow(() -> new IllegalArgumentException("테마가 없습니다."));
        Reservation reservation;
        if (request.getName() == null) {
            Member member = memberRepository.findById(loginMember.getId())
                    .orElseThrow(() -> new IllegalArgumentException("예약할 회원이 없습니다."));
            reservation = new Reservation(member, request.getDate(), time, theme);
        } else {
            if (request.getName().isBlank()) {
                throw new IllegalArgumentException("예약자 이름은 공백일 수 없습니다.");
            }
            reservation = new Reservation(request.getName(), request.getDate(), time, theme);
        }
        reservation = reservationRepository.save(reservation);
        return toResponse(reservation);
    }

    @Transactional
    public void deleteById(Long id) {
        reservationRepository.deleteById(id);
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAllByOrderByIdAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<MyReservationResponse> findMine(LoginMember loginMember) {
        List<MyReservationResponse> responses = new ArrayList<>(
                reservationRepository.findByMemberIdOrderByIdAsc(loginMember.getId()).stream()
                        .map(reservation -> new MyReservationResponse(reservation.getId(),
                                reservation.getTheme().getName(), reservation.getDate(),
                                reservation.getTime().getValue(), "예약"))
                        .toList());
        waitingRepository.findWaitingsWithRankByMemberId(loginMember.getId()).forEach(waitingWithRank -> {
            Waiting waiting = waitingWithRank.getWaiting();
            responses.add(new MyReservationResponse(waiting.getId(), null,
                    waiting.getTheme().getName(), waiting.getDate(), waiting.getTime().getValue(),
                    (waitingWithRank.getRank() + 1) + "번째 예약대기"));
        });
        return responses;
    }

    private ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(reservation.getId(), reservation.getName(),
                reservation.getTheme().getName(), reservation.getDate(), reservation.getTime().getValue());
    }
}
