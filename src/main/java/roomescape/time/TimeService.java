package roomescape.time;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationRepository;

import java.util.List;

@Service
public class TimeService {
    private final TimeRepository timeRepository;
    private final ReservationRepository reservationRepository;

    public TimeService(TimeRepository timeRepository, ReservationRepository reservationRepository) {
        this.timeRepository = timeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<AvailableTime> getAvailableTime(String date, Long themeId) {
        List<Reservation> reservations = reservationRepository.findByDateAndThemeId(date, themeId);
        List<Time> times = timeRepository.findAllByDeletedFalse();

        return times.stream()
                .map(time -> new AvailableTime(
                        time.getId(),
                        time.getValue(),
                        reservations.stream()
                                .anyMatch(reservation -> reservation.getTime().getId().equals(time.getId()))
                ))
                .toList();
    }

    public List<Time> findAll() {
        return timeRepository.findAllByDeletedFalse();
    }

    public Time save(TimeRequest request) {
        Time time = new Time(request.value());

        return timeRepository.save(time);
    }

    @Transactional
    public void deleteById(Long id) {
        Time time = timeRepository.findById(id).orElseThrow();

        time.delete();
    }
}
