package roomescape.reservation.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.LoginMember;
import roomescape.exception.auth.ForbiddenAdminOperationException;
import roomescape.exception.member.NotFoundMemberException;
import roomescape.exception.reservation.DuplicateReservationException;
import roomescape.exception.reservation.ForbiddenReservationException;
import roomescape.exception.reservation.InvalidReservationException;
import roomescape.exception.reservation.NotFoundReservationException;
import roomescape.exception.theme.NotFoundThemeException;
import roomescape.exception.time.NotFoundTimeException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.Time;
import roomescape.time.repository.TimeRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.WaitingRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final TimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(ReservationRepository reservationRepository, MemberRepository memberRepository, TimeRepository timeRepository, ThemeRepository themeRepository, WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.waitingRepository = waitingRepository;
    }

    public ReservationResponse save(LoginMember loginMember, ReservationRequest reservationRequest) {
        Member member = memberRepository.findById(loginMember.id())
                .orElseThrow(() -> new NotFoundMemberException("예약할 회원을 찾을 수 없습니다."));

        String reservationName = member.getName();
        String requestedName = reservationRequest.getName();

        if (requestedName != null && loginMember.role() != Role.ADMIN) {
            throw new ForbiddenAdminOperationException("관리자만 예약자를 지정할 수 있습니다.");
        }

        if (requestedName != null) {
            if (requestedName.isBlank()) {
                throw new InvalidReservationException("예약자 이름을 올바르게 입력해야 합니다.");
            }

            member = memberRepository.findByName(requestedName)
                    .orElseThrow(() -> new NotFoundMemberException("예약할 회원을 찾을 수 없습니다."));

            reservationName = member.getName();
        }

        Time time = timeRepository.findByIdAndDeletedFalse(reservationRequest.getTime())
                .orElseThrow(() -> new NotFoundTimeException("예약 시간을 찾을 수 없습니다."));

        Theme theme = themeRepository.findByIdAndDeletedFalse(reservationRequest.getTheme())
                .orElseThrow(() -> new NotFoundThemeException("예약 테마를 찾을 수 없습니다."));

        Reservation newReservation = Reservation.create(
                reservationName,
                reservationRequest.getDate(),
                time,
                theme,
                member
        );

        Reservation savedReservation;

        try {
            savedReservation = reservationRepository.save(newReservation);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateReservationException("이미 해당 날짜와 시간에 예약된 테마입니다.");
        }

        return new ReservationResponse(savedReservation.getId(), savedReservation.getName(), savedReservation.getTheme().getName(), savedReservation.getDate(), savedReservation.getTime().getValue());
    }

    @Transactional
    public void deleteById(LoginMember loginMember, Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundReservationException("삭제할 예약을 찾을 수 없습니다."));

        if (!reservation.getMember().getId().equals(loginMember.id())
                && loginMember.role() != Role.ADMIN) {
            throw new ForbiddenReservationException("본인의 예약만 취소할 수 있습니다.");
        }

        LocalDateTime reservationDateTime = LocalDateTime.of(LocalDate.parse(reservation.getDate()), LocalTime.parse(reservation.getTime().getValue()));

        if (!reservationDateTime.isAfter(LocalDateTime.now())) {
            waitingRepository.deleteAllByDateAndThemeIdAndTimeId(
                    reservation.getDate(),
                    reservation.getTheme().getId(),
                    reservation.getTime().getId()
            );
            reservationRepository.delete(reservation);
            return;
        }

        Optional<Waiting> firstWaiting = waitingRepository.findFirstByDateAndThemeIdAndTimeIdOrderByIdAsc(reservation.getDate(), reservation.getTheme().getId(), reservation.getTime().getId());

        reservationRepository.delete(reservation);

        reservationRepository.flush();

        firstWaiting.ifPresent(waiting -> {
            Reservation promoted = Reservation.create(
                    waiting.getMember().getName(),
                    waiting.getDate(),
                    waiting.getTime(),
                    waiting.getTheme(),
                    waiting.getMember()
            );
            reservationRepository.save(promoted);
            waitingRepository.delete(waiting);
        });
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll().stream()
                .map(it -> new ReservationResponse(it.getId(), it.getName(), it.getTheme().getName(), it.getDate(), it.getTime().getValue()))
                .toList();
    }

    public List<MyReservationResponse> findMyReservations(LoginMember loginMember) {
        List<MyReservationResponse> reservations = reservationRepository
                .findAllByMemberId(loginMember.id())
                .stream()
                .map(MyReservationResponse::from)
                .toList();

        List<MyReservationResponse> waitings = waitingRepository
                .findWaitingsWithRankByMemberId(loginMember.id())
                .stream()
                .map(MyReservationResponse::from)
                .toList();

        return Stream.concat(reservations.stream(), waitings.stream())
                .toList();
    }
}
