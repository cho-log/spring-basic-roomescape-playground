package roomescape.reservation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.UnauthorizedException;
import roomescape.member.LoginMember;
import roomescape.member.Member;
import roomescape.theme.Theme;
import roomescape.time.Time;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void 본인의_예약이_아니고_관리자가_아니면_삭제시_예외가_발생한다() {
        // given
        Member owner = new Member(1L, "소유자", "owner@email.com", "USER");
        LoginMember other = new LoginMember(2L, "타인", "other@email.com", "USER");
        Time time = new Time(1L, "10:00");
        Theme theme = new Theme(1L, "테마", "설명");
        Reservation reservation = new Reservation(10L, "소유자", "2026-03-28", time, theme, owner);

        given(reservationRepository.findById(10L)).willReturn(Optional.of(reservation));

        // when & then
        assertThatThrownBy(() -> reservationService.deleteById(10L, other))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("본인의 예약이거나 관리자만 삭제할 수 있습니다.");
    }

    @Test
    void 관리자는_타인의_예약도_삭제할_수_있다() {
        // given
        Member owner = new Member(1L, "소유자", "owner@email.com", "USER");
        LoginMember admin = new LoginMember(99L, "관리자", "admin@email.com", "ADMIN");
        Time time = new Time(1L, "10:00");
        Theme theme = new Theme(1L, "테마", "설명");
        Reservation reservation = new Reservation(10L, "소유자", "2026-03-28", time, theme, owner);

        given(reservationRepository.findById(10L)).willReturn(Optional.of(reservation));

        // when
        reservationService.deleteById(10L, admin);

        // then
        verify(reservationRepository).deleteById(10L);
    }
}
