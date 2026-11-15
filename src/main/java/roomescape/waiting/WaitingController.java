package roomescape.waiting;

import auth.LoginMember;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/waitings")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> create(@RequestBody WaitingRequest waitingRequest, LoginMember loginMember) {
        if (waitingRequest.date() == null
                || waitingRequest.theme() == null
                || waitingRequest.time() == null) {
            return ResponseEntity.badRequest().build();
        }

        WaitingResponse waiting = waitingService.save(waitingRequest, loginMember);

        return ResponseEntity.created(URI.create("/waitings/" + waiting.id())).body(waiting);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, LoginMember loginMember) {
        waitingService.deleteById(id, loginMember);
        return ResponseEntity.noContent().build();
    }
}
