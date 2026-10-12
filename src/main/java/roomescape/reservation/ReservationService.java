package roomescape.reservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.DuplicateException;
import roomescape.exception.NotFoundException;
import roomescape.exception.UnauthorizedException;
import roomescape.member.LoginMember;
import roomescape.member.Member;
import roomescape.theme.Theme;
import roomescape.theme.ThemeRepository;
import roomescape.time.Time;
import roomescape.time.TimeRepository;
import roomescape.waiting.WaitingRepository;
import roomescape.waiting.WaitingWithRank;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;
    private final TimeRepository timeRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(ReservationRepository reservationRepository, ThemeRepository themeRepository, TimeRepository timeRepository, WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
        this.timeRepository = timeRepository;
        this.waitingRepository = waitingRepository;
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll().stream()
                .map(it -> new ReservationResponse(
                        it.getId(),
                        resolveName(it),
                        it.getTheme().getName(),
                        it.getDate(),
                        it.getTime().getTime()
                ))
                .toList();
    }

    @Transactional
    public ReservationResponse save(ReservationRequest reservationRequest, Member member) {
        Theme theme = themeRepository.findById(reservationRequest.getTheme())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다."));
        Time time = timeRepository.findById(reservationRequest.getTime())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 시간입니다."));

        if (reservationRepository.existsByDateAndTheme_IdAndTime_Id(
                reservationRequest.getDate(), reservationRequest.getTheme(), reservationRequest.getTime())) {
            throw new DuplicateException("이미 예약된 시간입니다.");
        }

        Reservation reservation = new Reservation(reservationRequest.getName(), reservationRequest.getDate(), time, theme, member);
        Reservation saved = reservationRepository.save(reservation);

        return new ReservationResponse(saved.getId(), resolveName(saved), saved.getTheme().getName(), saved.getDate(), saved.getTime().getTime());
    }

    // 7단계 수정) DB 조회 없이 ArgumentResolver가 토큰에서 꺼내온 LoginMember로 권한을 검사
    @Transactional
    public void deleteById(Long id, LoginMember loginMember) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약입니다."));

        if (!reservation.isOwnedBy(loginMember.getId()) && !loginMember.isAdmin()) {
            throw new UnauthorizedException("본인의 예약이거나 관리자만 삭제할 수 있습니다.");
        }

        reservationRepository.deleteById(id);
    }

    public List<MyReservationResponse> findMyReservations(Long memberId) {
        // 1) 확정된 예약 목록
        List<MyReservationResponse> reservations = reservationRepository.findByMember_Id(memberId).stream()
                .map(it -> new MyReservationResponse(
                        it.getId(),
                        it.getTheme().getName(),
                        it.getDate(),
                        it.getTime().getTime(),
                        "예약"))
                .toList();
        // 2) 대기중인 예약 목록(순위 포함)
        List<MyReservationResponse> waitings = waitingRepository.findWaitingsWithRankByMemberId(memberId).stream()
                .map(this::toWaitingResponse)
                .toList();

        // 3) 예약, 대기 합치기
        List<MyReservationResponse> result = new ArrayList<>();
        result.addAll(reservations);
        result.addAll(waitings);
        return result;
    }

    private MyReservationResponse toWaitingResponse(WaitingWithRank it) {
        String status = (it.getRank() + 1) + "번째 예약대기";
        return new MyReservationResponse(
                it.getWaiting().getId(),
                it.getWaiting().getTheme().getName(),
                it.getWaiting().getDate(),
                it.getWaiting().getTime().getTime(),
                status);
    }

    // member가 연결되어 있으면 member의 실제 이름을, 없으면(관리자 대리예약) name 필드를 사용
    private String resolveName(Reservation r) {
        if (r.getMember() != null) {
            return r.getMember().getName();
        }
        return r.getName();
    }
}
