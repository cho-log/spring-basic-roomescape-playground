package roomescape.waiting.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.LoginMember;
import roomescape.exception.reservation.InvalidReservationException;
import roomescape.waiting.dto.WaitingRequest;
import roomescape.waiting.dto.WaitingResponse;
import roomescape.waiting.service.WaitingService;

import java.net.URI;

@RestController
public class WaitingController {
    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping("/waitings")
    public ResponseEntity<WaitingResponse> create(LoginMember loginMember, @RequestBody WaitingRequest waitingRequest) {
        if (waitingRequest.date() == null
                || waitingRequest.date().isBlank()
                || waitingRequest.theme() == null
                || waitingRequest.time() == null) {
            throw new InvalidReservationException("예약 대기 정보를 모두 입력해야 합니다.");
        }

        WaitingResponse waiting = waitingService.save(loginMember, waitingRequest);

        return ResponseEntity.created(URI.create("/waitings/" + waiting.getId())).body(waiting);
    }

    @DeleteMapping("/waitings/{id}")
    public ResponseEntity<Void> delete(LoginMember loginMember, @PathVariable Long id) {
        waitingService.deleteById(loginMember, id);
        return ResponseEntity.noContent().build();
    }
}
