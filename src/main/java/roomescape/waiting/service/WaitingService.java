package roomescape.waiting.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import roomescape.auth.LoginMember;
import roomescape.exception.member.NotFoundMemberException;
import roomescape.exception.reservation.InvalidReservationException;
import roomescape.exception.theme.NotFoundThemeException;
import roomescape.exception.time.NotFoundTimeException;
import roomescape.exception.waiting.DuplicateWaitingException;
import roomescape.exception.waiting.ForbiddenWaitingException;
import roomescape.exception.waiting.NotFoundWaitingException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.Time;
import roomescape.time.repository.TimeRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.dto.WaitingRequest;
import roomescape.waiting.dto.WaitingResponse;
import roomescape.waiting.repository.WaitingRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@Service
public class WaitingService {
    private final WaitingRepository waitingRepository;
    private final MemberRepository memberRepository;
    private final TimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public WaitingService(WaitingRepository waitingRepository, MemberRepository memberRepository, TimeRepository timeRepository, ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.waitingRepository = waitingRepository;
        this.memberRepository = memberRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public WaitingResponse save(LoginMember loginMember, WaitingRequest waitingRequest) {
        Member member = memberRepository.findById(loginMember.id())
                .orElseThrow(() -> new NotFoundMemberException("예약 대기할 회원을 찾을 수 없습니다."));

        Time time = timeRepository.findByIdAndDeletedFalse(waitingRequest.time())
                .orElseThrow(() -> new NotFoundTimeException("예약 대기 시간을 찾을 수 없습니다."));

        validateFutureDateTime(waitingRequest.date(), time);

        Theme theme = themeRepository.findByIdAndDeletedFalse(waitingRequest.theme())
                .orElseThrow(() -> new NotFoundThemeException("예약 대기 테마를 찾을 수 없습니다."));

        Reservation reservation = reservationRepository
                .findByDateAndThemeIdAndTimeId(
                        waitingRequest.date(), theme.getId(), time.getId()
                )
                .orElseThrow(() -> new InvalidReservationException(
                        "예약되지 않은 시간에는 대기를 신청할 수 없습니다."
                ));

        if (reservation.getMember().getId().equals(member.getId())) {
            throw new InvalidReservationException(
                    "본인의 예약에는 대기를 신청할 수 없습니다."
            );
        }

        Waiting waiting;

        try {
            waiting = waitingRepository.save(new Waiting(member, theme, waitingRequest.date(), time));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateWaitingException("이미 신청한 예약 대기입니다.");
        }

        long waitingNumber = waitingRepository.countByDateAndThemeIdAndTimeIdAndIdLessThan(
                waitingRequest.date(),
                waitingRequest.theme(),
                waitingRequest.time(),
                waiting.getId()
        ) + 1;

        return new WaitingResponse(waiting.getId(), waitingNumber);
    }

    public void deleteById(LoginMember loginMember, Long id) {
        Waiting waiting = waitingRepository.findById(id)
                .orElseThrow(() -> new NotFoundWaitingException("취소할 대기중인 예약을 찾을 수 없습니다."));

        if (!waiting.getMember().getId().equals(loginMember.id())) {
            throw new ForbiddenWaitingException("본인의 예약 대기만 취소할 수 있습니다.");
        }
        waitingRepository.delete(waiting);
    }

    private void validateFutureDateTime(String date, Time time) {
        if (date == null) {
            throw new InvalidReservationException("올바른 예약 날짜와 시간을 선택해야 합니다.");
        }

        try {
            LocalDateTime reservationDateTime = LocalDateTime.of(
                    LocalDate.parse(date),
                    LocalTime.parse(time.getValue())
            );

            if (!reservationDateTime.isAfter(LocalDateTime.now())) {
                throw new InvalidReservationException("지난 예약에는 대기를 신청할 수 없습니다.");
            }
        } catch (DateTimeParseException exception) {
            throw new InvalidReservationException("올바른 예약 날짜와 시간을 선택해야 합니다.");
        }
    }
}
