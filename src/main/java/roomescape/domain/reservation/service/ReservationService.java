package roomescape.domain.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.entity.Member;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.domain.reservation.entity.Reservation;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.theme.repository.ThemeRepository;
import roomescape.domain.time.entity.Time;
import roomescape.domain.time.repository.TimeRepository;
import roomescape.domain.waiting.repository.ReserveWaitingRepository;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.NotFoundException;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    private final ReserveWaitingRepository reserveWaitingRepository;
    private final ThemeRepository themeRepository;
    private final TimeRepository timeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(ReservationRepository reservationRepository, ReserveWaitingRepository reserveWaitingRepository, ThemeRepository themeRepository, TimeRepository timeRepository, MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.reserveWaitingRepository = reserveWaitingRepository;
        this.themeRepository = themeRepository;
        this.timeRepository = timeRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Reservation createReservationByAdmin(String nickname, LocalDate date, Long themeId, Long timeId) {

        Member foundMember = memberRepository.findByNicknameForUpdate(nickname)
                .orElseThrow(() -> new NotFoundException("해당하는 사용자를 찾을 수 없습니다."));
        Theme foundTheme = themeRepository.getReferenceById(themeId);
        Time foundTime = timeRepository.getReferenceById(timeId);


        if (reserveWaitingRepository.existsByMemberAndDateAndTimeAndTheme(foundMember, date, foundTime, foundTheme)) {
            throw new ConflictException("이미 예약 대기 중입니다.");
        }

        return reservationRepository.save(new Reservation(date, foundMember, foundTime, foundTheme));
    }

    @Transactional
    public Reservation createReservationByUser(Long memberId, LocalDate date, Long themeId, Long timeId) {

        Member foundMember = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다."));
        Theme foundTheme = themeRepository.getReferenceById(themeId);
        Time foundTime = timeRepository.getReferenceById(timeId);

        if (reserveWaitingRepository.existsByMemberAndDateAndTimeAndTheme(foundMember, date, foundTime, foundTheme)) {
            throw new ConflictException("이미 예약 대기중입니다.");
        }

        return reservationRepository.save(new Reservation(date, foundMember, foundTime, foundTheme));
    }

    public List<Reservation> findAllReservationByUser(Long memberId) {
        return reservationRepository.findAllByMember_Id(memberId);
    }

    @Transactional
    public void deleteById(Long reservationId) {
        Reservation foundReservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("해당하는 예약을 찾을 수 없습니다."));

        reservationRepository.delete(foundReservation);
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }
}
