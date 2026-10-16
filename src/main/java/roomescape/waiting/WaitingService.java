package roomescape.waiting;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import roomescape.member.Member;
import roomescape.reservation.ReservationRequest;
import roomescape.theme.Theme;
import roomescape.theme.ThemeRepository;
import roomescape.time.Time;
import roomescape.time.TimeRepository;
import roomescape.reservation.ReservationRepository;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final TimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public WaitingService(
            WaitingRepository waitingRepository,
            TimeRepository timeRepository,
            ThemeRepository themeRepository,
            ReservationRepository reservationRepository
    ) {
        this.waitingRepository = waitingRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public WaitingResponse save(ReservationRequest request, Member member) {
        Time time = timeRepository.findById(request.getTime()).orElseThrow();
        Theme theme = themeRepository.findById(request.getTheme()).orElseThrow();

        if (time.isDeleted()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "삭제된 시간은 예약 대기할 수 없습니다."
            );
        }

        if (theme.isDeleted()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "삭제된 테마는 예약 대기할 수 없습니다."
            );
        }

        boolean reservationExists = reservationRepository.existsByDateAndTimeIdAndThemeId(
                request.getDate(),
                request.getTime(),
                request.getTheme()
        );

        if (!reservationExists) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "예약이 존재하는 시간에만 예약 대기를 신청할 수 있습니다."
            );
        }

        boolean exists = waitingRepository.existsByMemberIdAndDateAndTimeIdAndThemeId(
                member.getId(),
                request.getDate(),
                request.getTime(),
                request.getTheme()
        );

        if (exists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 예약 대기 중입니다."
            );
        }

        Waiting waiting = new Waiting(
                request.getDate(),
                time,
                theme,
                member
        );

        Waiting saveWaiting = waitingRepository.save(waiting);

        return new WaitingResponse(
                saveWaiting.getId(),
                theme.getName(),
                saveWaiting.getDate(),
                time.getValue(),
                "예약대기"
        );
    }

    public void delete(Long waitingId, Long memberId) {
        Waiting waiting = waitingRepository.findById(waitingId).orElseThrow();

        if (!waiting.getMember().getId().equals(memberId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        waitingRepository.delete(waiting);
    }

}
