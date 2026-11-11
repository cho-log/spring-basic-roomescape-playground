package roomescape.waiting;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.LoginMember;

@RestController
public class WaitingController {
    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping("/waitings")
    public ResponseEntity<WaitingResponse> create(@Valid @RequestBody WaitingRequest request, LoginMember loginMember) {
        WaitingResponse waiting = waitingService.save(request, loginMember);
        return ResponseEntity.created(URI.create("/waitings/" + waiting.getId())).body(waiting);
    }

    @DeleteMapping("/waitings/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id, LoginMember loginMember) {
        waitingService.cancel(id, loginMember);
        return ResponseEntity.noContent().build();
    }
}
