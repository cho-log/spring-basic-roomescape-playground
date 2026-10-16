package roomescape.reservation;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import roomescape.member.Member;
import roomescape.theme.Theme;
import roomescape.theme.ThemeRepository;
import roomescape.time.Time;
import roomescape.time.TimeRepository;
import roomescape.waiting.WaitingRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final TimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(ReservationRepository reservationRepository, TimeRepository timeRepository, ThemeRepository themeRepository, WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.waitingRepository = waitingRepository;
    }

    public ReservationResponse save(ReservationRequest reservationRequest, Member member) {
        Time time = timeRepository.findById(reservationRequest.getTime()).orElseThrow();
        Theme theme = themeRepository.findById(reservationRequest.getTheme()).orElseThrow();

        if (time.isDeleted()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "삭제된 시간은 예약할 수 없습니다."
            );
        }

        if (theme.isDeleted()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "삭제된 테마는 예약할 수 없습니다."
            );
        }

        boolean exists = reservationRepository.existsByDateAndTimeIdAndThemeId(
                reservationRequest.getDate(),
                reservationRequest.getTime(),
                reservationRequest.getTheme()
        );

        if (exists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 예약된 날짜와 시간입니다."
            );
        }

        String name;
        if (member != null) {
            name = member.getName();
        }
        else {
            name = reservationRequest.getName();
        }

        Reservation reservation = new Reservation(
                name,
                reservationRequest.getDate(),
                time,
                theme,
                member
        );

        Reservation saveReservation = reservationRepository.save(reservation);

        return new ReservationResponse(saveReservation.getId(), name, reservation.getTheme().getName(), reservation.getDate(), reservation.getTime().getValue());
    }

    public void deleteById(Long id, Long memberId, String role) {
        Reservation reservation = reservationRepository.findById(id).orElseThrow();

        // 관리자 대리 예약일 때, 관리자면 삭제 가능, 일반 회원이면 403
        if (reservation.getMember() == null) {
            if (!role.equals("ADMIN")) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }
        // 일반 회원일 때, 본인은 삭제 가능, 다른 회원이면 403, 관리자지만 다른 회원이면 403
        else if (!reservation.getMember().getId().equals(memberId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        reservationRepository.deleteById(id);
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll().stream()
                .map(it -> new ReservationResponse(it.getId(), it.getName(), it.getTheme().getName(), it.getDate(), it.getTime().getValue()))
                .toList();
    }

    public List<MyReservationResponse> findMyReservations(Long memberId) {
        List<MyReservationResponse> reservations = reservationRepository.findByMemberId(memberId).stream()
                .map(it -> new MyReservationResponse(
                        it.getId(),
                        it.getTheme().getName(),
                        it.getDate(),
                        it.getTime().getValue(),
                        "예약"))
                .toList();

        List<MyReservationResponse> waitings = waitingRepository
                .findWaitingsWithRankByMemberId(memberId).stream()
                .map(it -> new MyReservationResponse(
                        it.getWaiting().getId(),
                        it.getWaiting().getTheme().getName(),
                        it.getWaiting().getDate(),
                        it.getWaiting().getTime().getValue(),
                        (it.getRank() + 1) + "번째 예약대기"
                )).toList();

        List<MyReservationResponse> result = new ArrayList<>(reservations);
        result.addAll(waitings);

        return result;
    }
}
