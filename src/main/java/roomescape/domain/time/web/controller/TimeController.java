package roomescape.domain.time.web.controller;

import auth.support.annotation.AdminOnly;
import auth.support.annotation.Public;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import roomescape.domain.time.entity.AvailableTime;
import roomescape.domain.time.entity.Time;
import roomescape.domain.time.service.TimeService;
import roomescape.domain.time.web.dto.TimeRequest;
import roomescape.domain.time.web.dto.TimeResponse;
import roomescape.global.exception.BadRequestException;
import roomescape.global.exception.ConflictException;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
public class TimeController {
    private final TimeService timeService;

    public TimeController(TimeService timeService) {
        this.timeService = timeService;
    }

    @Public
    @GetMapping("/times")
    public List<TimeResponse> list() {
        return timeService.findAll().stream().map(TimeResponse::from).toList();
    }

    @AdminOnly
    @PostMapping("/times")
    public ResponseEntity<TimeResponse> create(
            @Valid @RequestBody TimeRequest request
    ) {
        Time newTime;

        try {
            newTime = timeService.save(request.value());
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("이미 존재하는 시각입니다.");
        }

        return ResponseEntity.created(URI.create("/times/" + newTime.getId())).body(TimeResponse.from(newTime));
    }

    @AdminOnly
    @DeleteMapping("/times/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            timeService.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("해당 시각으로 예약 혹은 예약 대기된 건이 있습니다. 해당 건을 삭제한 후 다시 시도하여 주세요.");
        } catch (OptimisticLockingFailureException e) {
            // 삭제 동시 요청의 경우, 이미 삭제된 리소스에 대한 추가 삭제는 예외 반환이 필요 없다 판단.
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.noContent().build();
    }

    @Public
    @GetMapping("/available-times")
    public ResponseEntity<List<AvailableTime>> availableTimes(
            @RequestParam @FutureOrPresent(message = "날짜는 과거일 수 없습니다.") LocalDate date,
            @RequestParam Long themeId
    ) {
        return ResponseEntity.ok(timeService.getAvailableTime(date, themeId));
    }
}
