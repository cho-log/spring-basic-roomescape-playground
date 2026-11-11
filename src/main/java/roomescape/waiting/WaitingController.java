package roomescape.waiting;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginMember;

@RestController
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping("/waitings")
    public ResponseEntity create(@Valid @RequestBody WaitingRequest waitingRequest, LoginMember loginMember) {
        WaitingResponse waiting = waitingService.save(waitingRequest, loginMember);

        return ResponseEntity.status(HttpStatus.CREATED).body(waiting);
    }

    @DeleteMapping("/waitings/{id}")
    public ResponseEntity delete(@PathVariable Long id, LoginMember loginMember) {
        waitingService.deleteById(id, loginMember);
        return ResponseEntity.noContent().build();
    }
}
