package roomescape.reservation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.LoginMember;
import roomescape.member.Member;
import roomescape.member.MemberRepository;

import java.net.URI;
import java.util.List;

@RestController
public class ReservationController {

    private final ReservationService reservationService;
    private final MemberRepository memberRepository;

    public ReservationController(ReservationService reservationService, MemberRepository memberRepository) {
        this.reservationService = reservationService;
        this.memberRepository = memberRepository;
    }

    @GetMapping("/reservations")
    public List<ReservationResponse> list() {
        return reservationService.findAll();
    }

    @PostMapping("/reservations")
    public ResponseEntity create(@RequestBody ReservationRequest reservationRequest, LoginMember loginMember) {
        if (reservationRequest.getDate() == null
                || reservationRequest.getTheme() == null
                || reservationRequest.getTime() == null) {
            return ResponseEntity.badRequest().build();
        }

        String requestName = reservationRequest.getName();
        // 사용자의 요청에는 이름이 들어있지 않음(id로 db조회 필요)
        // 관리자가 보낸 예약 생성 요청에는 이름이 들어있어 db조회 X
        boolean isSelfBooking = requestName == null || requestName.isBlank();

        String reservationName = isSelfBooking ? loginMember.getName() : requestName;
        Member member = isSelfBooking
                ? memberRepository.findById(loginMember.getId()).orElseThrow()
                : memberRepository.findByName(requestName).orElse(null);

        ReservationRequest requestWithName = new ReservationRequest(
                reservationName,
                reservationRequest.getDate(),
                reservationRequest.getTheme(),
                reservationRequest.getTime()
        );

        ReservationResponse reservation = reservationService.save(requestWithName, member);
        return ResponseEntity.created(URI.create("/reservations/" + reservation.getId())).body(reservation);
    }

    @GetMapping("/reservations-mine")
    public List<MyReservationResponse> myReservations(LoginMember loginMember) {
        return reservationService.findMyReservations(loginMember.getId());
    }

    // 7단계) DB 조회 삭제
    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, LoginMember loginMember) {
        reservationService.deleteById(id, loginMember);
        return ResponseEntity.noContent().build();
    }
}
