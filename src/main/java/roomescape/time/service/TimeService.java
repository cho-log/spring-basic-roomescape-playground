package roomescape.time.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import roomescape.exception.time.DuplicateTimeException;
import roomescape.exception.time.NotFoundTimeException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.time.domain.Time;
import roomescape.time.dto.AvailableTime;
import roomescape.time.repository.TimeRepository;

import java.util.List;
import java.util.Optional;

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

    public Time save(Time time) {
        Optional<Time> existingTime = timeRepository.findByValue(time.getValue());

        if (existingTime.isPresent()) {
            Time foundTime = existingTime.get();

            if (!foundTime.isDeleted()) {
                throw new DuplicateTimeException("이미 등록된 시간입니다.");
            }

            foundTime.restore();
            return timeRepository.save(foundTime);
        }

        try {
            return timeRepository.save(time);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateTimeException("이미 등록된 시간입니다.");
        }
    }

    public void deleteById(Long id) {
        Time time = timeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundTimeException("삭제할 시간을 찾을 수 없습니다."));

        time.delete();
        timeRepository.save(time);
    }
}
