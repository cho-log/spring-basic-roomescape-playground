package roomescape.domain.waiting.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.entity.Member;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.theme.repository.ThemeRepository;
import roomescape.domain.time.entity.Time;
import roomescape.domain.time.repository.TimeRepository;
import roomescape.domain.waiting.entity.ReserveWaiting;
import roomescape.domain.waiting.repository.ReserveWaitingRepository;
import roomescape.domain.waiting.service.result.WaitingWithRank;
import roomescape.global.exception.BadRequestException;
import roomescape.global.exception.NotFoundException;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReserveWaitingService {

    private final ReserveWaitingRepository reserveWaitingRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;
    private final TimeRepository timeRepository;

    public ReserveWaitingService(
            ReserveWaitingRepository reserveWaitingRepository,
            ReservationRepository reservationRepository,
            MemberRepository memberRepository,
            ThemeRepository themeRepository,
            TimeRepository timeRepository
    ) {
        this.reserveWaitingRepository = reserveWaitingRepository;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.themeRepository = themeRepository;
        this.timeRepository = timeRepository;
    }

    public List<WaitingWithRank> findAllMemberReserveWaits(Long memberId) {
        return reserveWaitingRepository.findWaitingWithRankByMemberId(memberId);
    }

    @Transactional
    public WaitingWithRank createReserveWaiting(Long memberId, LocalDate date, Long timeId, Long themeId) {

        Member foundMember = getMember(memberId);
        Time foundTime = getTime(timeId);
        Theme foundTheme = getTheme(themeId);

        if (reservationRepository.existsByMemberAndDateAndTimeAndTheme(foundMember, date, foundTime, foundTheme)) {
            throw new BadRequestException("이미 예약 중입니다.");
        }

        ReserveWaiting newReserveWaiting = reserveWaitingRepository.save(new ReserveWaiting(foundMember, date, foundTime, foundTheme));
        Long aheadCount = reserveWaitingRepository.countByDateAndTimeAndThemeAndIdLessThan(date, foundTime, foundTheme, newReserveWaiting.getId());

        return new WaitingWithRank(newReserveWaiting, aheadCount);
    }

    @Transactional
    public void deleteReserveWaiting(Long memberId, Long reserveWaitingId) {
        ReserveWaiting foundReserveWaiting = getReserveWaiting(memberId, reserveWaitingId);
        reserveWaitingRepository.delete(foundReserveWaiting);
    }

    private ReserveWaiting getReserveWaiting(Long memberId, Long reserveWaitingId) {
        return reserveWaitingRepository.findByIdAndMemberId(reserveWaitingId, memberId)
                .orElseThrow(() -> new NotFoundException("해당하는 예약 대기를 찾을 수 없습니다."));
    }

    private Member getMember(Long memberId) {
        return memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다."));
    }

    private Time getTime(Long timeId) {
        return timeRepository.getReferenceById(timeId);
    }

    private Theme getTheme(Long themeId) {
        return themeRepository.getReferenceById(themeId);
    }
}
