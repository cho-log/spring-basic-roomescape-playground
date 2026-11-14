package roomescape.waiting;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.LoginMember;
import roomescape.reservation.ReservationRepository;
import roomescape.theme.Theme;
import roomescape.theme.ThemeRepository;
import roomescape.time.Time;
import roomescape.time.TimeRepository;

@Service
@Transactional(readOnly = true)
public class WaitingService {
    private WaitingRepository waitingRepository;
    private TimeRepository timeRepository;
    private ThemeRepository themeRepository;
    private ReservationRepository reservationRepository;

    public WaitingService(WaitingRepository waitingRepository, TimeRepository timeRepository,
                           ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.waitingRepository = waitingRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public WaitingResponse save(WaitingRequest request, LoginMember loginMember) {
        Time time = timeRepository.findByIdAndDeletedFalse(request.getTime()).orElseThrow();
        Theme theme = themeRepository.findByIdAndDeletedFalse(request.getTheme()).orElseThrow();

        if (!reservationRepository.existsByThemeAndDateAndTime(theme, request.getDate(), time)) {
            throw new IllegalStateException("예약이 존재하지 않는 시간에는 대기를 신청할 수 없습니다.");
        }

        Waiting waiting = waitingRepository.save(
                new Waiting(loginMember.getId(), request.getDate(), time, theme));

        return new WaitingResponse(waiting.getId());
    }

    @Transactional
    public void deleteById(Long id) {
        waitingRepository.deleteById(id);
    }
}