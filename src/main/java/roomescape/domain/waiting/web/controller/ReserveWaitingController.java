package roomescape.domain.waiting.web.controller;

import auth.principal.LoginMember;
import auth.support.annotation.Login;
import auth.support.annotation.LoginRequired;
import jakarta.validation.Valid;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.domain.waiting.service.ReserveWaitingService;
import roomescape.domain.waiting.service.result.WaitingWithRank;
import roomescape.domain.waiting.web.dto.WaitingRequest;
import roomescape.domain.waiting.web.dto.WaitingResponse;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.NotFoundException;

import java.net.URI;

@RestController
public class ReserveWaitingController {

    private final ReserveWaitingService reserveWaitingService;

    public ReserveWaitingController(ReserveWaitingService reserveWaitingService) {
        this.reserveWaitingService = reserveWaitingService;
    }

    @LoginRequired
    @PostMapping("/waitings")
    public ResponseEntity<WaitingResponse> postReserveWaiting(
            @Login LoginMember loginMember,
            @RequestBody @Valid WaitingRequest request
    ) {
        WaitingWithRank newReserveWaiting;

        try {
             newReserveWaiting = reserveWaitingService.createReserveWaiting(loginMember.id(), request.date(), request.time(), request.theme());
        } catch(DataIntegrityViolationException e) {
            // 제약 조건 위배 예외. 제약 조건 이름이 null이 아니면 lowercase로 할당, null이면 빈 문자열로 할당
            String name =
                    e.getCause()
                            instanceof ConstraintViolationException constraintViolation
                            && constraintViolation.getConstraintName() != null ? constraintViolation.getConstraintName().toLowerCase() : "";

            if (name.contains("uk_reserve_waiting_member_date_time_theme")) {
                throw new ConflictException("이미 예약 대기중입니다.");
            }
            if (name.contains("fk_reserve_waiting_theme")) {
                throw new NotFoundException("해당하는 테마를 찾을 수 없습니다.");
            }
            if (name.contains("fk_reserve_waiting_time")) {
                throw new NotFoundException("해당하는 시각을 찾을 수 없습니다.");
            }
            throw e;
        }

        return ResponseEntity.created(URI.create("/waitings/" + newReserveWaiting.reserveWaiting().getId()))
                .body(WaitingResponse.from(newReserveWaiting));
    }

    @LoginRequired
    @DeleteMapping("/waitings/{id}")
    public ResponseEntity<Void> deleteReserveWaiting(
            @Login LoginMember loginMember,
            @PathVariable(name = "id") Long reserveWaitingId
    ) {
        try {
            reserveWaitingService.deleteReserveWaiting(loginMember.id(), reserveWaitingId);
        } catch (OptimisticLockingFailureException e) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.noContent().build();
    }
}
