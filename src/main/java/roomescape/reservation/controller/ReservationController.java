package roomescape.reservation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginMember;
import roomescape.exception.auth.ForbiddenAdminOperationException;
import roomescape.exception.reservation.InvalidReservationException;
import roomescape.member.domain.Role;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.service.ReservationService;

import java.net.URI;
import java.util.List;

@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations")
    public List<ReservationResponse> list(LoginMember loginMember) {
        if (loginMember.role() != Role.ADMIN) {
            throw new ForbiddenAdminOperationException("관리자만 전체 예약 목록을 조회할 수 있습니다.");
        }

        return reservationService.findAll();
    }

    @GetMapping("/reservations-mine")
    public List<MyReservationResponse> findMyReservations(LoginMember loginMember) {
        return reservationService.findMyReservations(loginMember);
    }

    @PostMapping("/reservations")
    public ResponseEntity create(
            LoginMember loginMember,
            @RequestBody ReservationRequest reservationRequest) {
        if (reservationRequest.getDate() == null
                || reservationRequest.getDate().isBlank()
                || reservationRequest.getTheme() == null
                || reservationRequest.getTime() == null) {
            throw new InvalidReservationException("예약 정보를 모두 입력해야 합니다.");
        }
        ReservationResponse reservation = reservationService.save(loginMember, reservationRequest);

        return ResponseEntity.created(URI.create("/reservations/" + reservation.getId())).body(reservation);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> delete(LoginMember loginMember, @PathVariable Long id) {
        reservationService.deleteById(loginMember, id);
        return ResponseEntity.noContent().build();
    }
}
