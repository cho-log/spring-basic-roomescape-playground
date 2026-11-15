package roomescape.domain.reservation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import roomescape.domain.reservation.entity.Reservation;
import roomescape.domain.reservation.service.ReservationService;
import roomescape.global.data.SchemaInitializer;
import roomescape.global.data.SchemaInitializerDependency;
import roomescape.global.data.TestDataLoader;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.NotFoundException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({ReservationService.class, SchemaInitializer.class, SchemaInitializerDependency.class, TestDataLoader.class})
public class ReservationServiceTest {

    private final Long time = 1L;

    @Autowired
    private ReservationService reservationService;

    @Nested
    @DisplayName("관리자")
    class Admin {
        @Test
        void createReservationByAdmin을_존재하지_않는_themeId로_호출하면_예외가_발생한다() {
            // then
            Assertions.assertThrows(
                    DataIntegrityViolationException.class,

                    // when
                    () -> reservationService.createReservationByAdmin("더미_유저", LocalDate.of(9999, 12, 30), -1L, time)
            );
        }

        @Test
        void createReservationByAdmin을_존재하지_않는_time으로_호출하면_예외가_발생한다() {
            // then
            Assertions.assertThrows(
                    DataIntegrityViolationException.class,

                    // when
                    () -> reservationService.createReservationByAdmin("더미_유저", LocalDate.of(9999, 12, 30), 1L, -1L)
            );
        }

        @Test
        void createReservationByAdmin을_존재하지_않는_nickname으로_호출하면_예외가_발생한다() {
            // then
            Assertions.assertThrows(
                    NotFoundException.class,

                    // when
                    () -> reservationService.createReservationByAdmin("존재하지_않는_유저", LocalDate.of(9999, 12, 30), 1L, time)
            );
        }

        @Test
        void 이미_예약한_날짜_시간_테마에_예약을_생성하면_예외를_던진다() {
            // then
            Assertions.assertThrows(
                    DataIntegrityViolationException.class,
                    () -> reservationService.createReservationByAdmin("더미_어드민", LocalDate.of(9999, 12, 31), 1L, time)
            );
        }

        @Test
        void 예약자가_예약_대기_중인_날짜_시간_테마에_예약을_생성하면_예외를_던진다() {
            // data-test.sql
            Assertions.assertThrows(
                    ConflictException.class,
                    () -> reservationService.createReservationByAdmin("더미_유저", LocalDate.of(9999, 1, 1), 1L, time)
            );
        }

        @Test
        void 예약에_성공한다() {
            // given
            LocalDate reserveDate = LocalDate.of(9999, 12, 30);
            // when
            Reservation reservation = reservationService.createReservationByAdmin("더미_유저", reserveDate, 1L, time);

            // then
            assertThat(reservation).isNotNull();
            assertThat(reservation.getId()).isNotNull();
            assertThat(reservation.getDate()).isEqualTo(reserveDate);
        }
    }

    @Nested
    @DisplayName("일반 사용자")
    class User {
        @Test
        void createReservationByUser를_존재하지_않는_themeId로_호출하면_예외가_발생한다() {
            // then
            Assertions.assertThrows(
                    DataIntegrityViolationException.class,

                    // when
                    () -> reservationService.createReservationByUser(2L, LocalDate.of(9999, 12, 30), -1L, time)
            );
        }

        @Test
        void createReservationByUser를_존재하지_않는_time으로_호출하면_예외가_발생한다() {
            // then
            Assertions.assertThrows(
                    DataIntegrityViolationException.class,

                    // when
                    () -> reservationService.createReservationByUser(2L, LocalDate.of(9999, 12, 30), 1L, -1L)
            );
        }

        @Test
        void createReservationByUser를_존재하지_않는_memberId로_호출하면_예외가_발생한다() {
            // then
            Assertions.assertThrows(
                    NotFoundException.class,

                    // when
                    () -> reservationService.createReservationByUser(-1L, LocalDate.of(9999, 12, 30), 1L, time)
            );
        }

        @Test
        void 이미_예약한_날짜_시간_테마에_예약을_생성하면_예외를_던진다() {
            // then
            Assertions.assertThrows(
                    DataIntegrityViolationException.class,
                    () -> reservationService.createReservationByUser(2L, LocalDate.of(9999, 12, 31), 1L, time)
            );
        }

        @Test
        void 예약_대기_중인_날짜_시간_테마에_예약을_생성하면_예외를_던진다() {
            // data-test.sql
            Assertions.assertThrows(
                    ConflictException.class,
                    () -> reservationService.createReservationByUser(2L, LocalDate.of(9999, 1, 1), 1L, time)
            );
        }

        @Test
        void 예약에_성공한다() {
            // given
            LocalDate reserveDate = LocalDate.of(9999, 12, 30);
            // when
            Reservation reservation = reservationService.createReservationByUser(2L, reserveDate, 1L, time);

            // then
            assertThat(reservation).isNotNull();
            assertThat(reservation.getId()).isNotNull();
            assertThat(reservation.getDate()).isEqualTo(reserveDate);
        }
    }

    @Test
    void 존재하지_않는_예약_ID로_deleteById_호출_시_Not_Found_예외가_발생한다() {
        // given
        Long reservationId = -1L;

        // when
        Assertions.assertThrows(NotFoundException.class,
                () -> reservationService.deleteById(reservationId)
        );
    }

    @Test
    void 정상적으로_deleteById를_호출한_경우_예약이_삭제된다() {
        // when
        reservationService.deleteById(1L);

        // then
        List<Reservation> reservations = reservationService.findAllReservationByUser(2L);
        assertThat(reservations).isEmpty();
    }
}
