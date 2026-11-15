package roomescape.domain.waiting;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import roomescape.domain.member.entity.Member;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.theme.repository.ThemeRepository;
import roomescape.domain.time.entity.Time;
import roomescape.domain.time.repository.TimeRepository;
import roomescape.domain.waiting.entity.ReserveWaiting;
import roomescape.domain.waiting.repository.ReserveWaitingRepository;
import roomescape.global.data.SchemaInitializer;
import roomescape.global.data.SchemaInitializerDependency;
import roomescape.global.data.TestDataLoader;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({SchemaInitializer.class, SchemaInitializerDependency.class, TestDataLoader.class})
public class ReserveWaitingRepositoryTest {

    private String nickname = "Alice";
    private LocalDate date = LocalDate.of(9999, 1, 1);

    @Autowired
    private ReserveWaitingRepository reserveWaitingRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    void 이미_예약_대기를_걸어둔_날짜_시각_테마_멤버_조합으로_existsByMemberAndDateAndTimeAndTheme을_호출하면_true를_반환한다() {
        // given
        Member member = findMember("더미_유저");
        Time time = findTime(1L);
        Theme theme = findTheme(1L);

        // when
        boolean exists = reserveWaitingRepository.existsByMemberAndDateAndTimeAndTheme(member, date, time, theme);

        assertThat(exists).isTrue();
    }

    @Test
    void 내_예약_대기가_존재하지_않는_날짜_시각_테마_조합으로_existsByMemberAndDateAndTimeAndTheme을_호출하면_false를_반환한다() {
        // given
        Member member = findMember("더미_유저");
        Time time = findTime(2L);
        Theme theme = findTheme(1L);

        // when
        boolean exists = reserveWaitingRepository.existsByMemberAndDateAndTimeAndTheme(member, date, time, theme);

        assertThat(exists).isFalse();
    }

    @Test
    void 이미_예약_대기한_것을_다시_save하면_복합_유니크_제약_조건에_걸린다() {
        // given
        Member member = findMember("더미_유저");
        Time time = findTime(1L);
        Theme theme = findTheme(1L);

        ReserveWaiting reserveWaiting = new ReserveWaiting(member, date, time, theme);

        // then
        Assertions.assertThrows(
                DataIntegrityViolationException.class,
                () -> reserveWaitingRepository.save(reserveWaiting)
        );
    }

    @Test
    void 예약_대기_하지_않은_날짜_시각_테마_조합으로_save하면_MANAGED_상태가_된다() {
        // given
        Member member = findMember("더미_유저");
        Time time = saveTime();
        Theme theme = findTheme(1L);

        ReserveWaiting reserveWaiting = new ReserveWaiting(member, date, time, theme);

        // when
        ReserveWaiting saved = reserveWaitingRepository.save(reserveWaiting);

        // then
        assertThat(saved).isNotNull();
        assertThat(saved).isSameAs(reserveWaiting);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getMember()).isEqualTo(member);
        assertThat(saved.getDate()).isEqualTo(date);
        assertThat(saved.getTime()).isEqualTo(time);
        assertThat(saved.getTheme()).isEqualTo(theme);
    }

    @Test
    void countByDateAndTimeAndThemeAndIdLessThan을_호출하면_ReserveWaitingId보다_먼저_이_날짜_시각_테마로_예약_대기를_건_사람의_수를_반환받는다() {
        // given
        Member member = saveMember("Alice");
        Time time = findTime(1L);
        Theme theme = findTheme(1L);

        ReserveWaiting saved = reserveWaitingRepository.save(new ReserveWaiting(member, date, time, theme));

        // when
        Long aheadCount = reserveWaitingRepository.countByDateAndTimeAndThemeAndIdLessThan(date, time, theme, saved.getId());

        // then
        assertThat(aheadCount).isEqualTo(1);
    }

    @Test
    void 예약_대기를_건_적_없는_member로_다른_member의_ReserveWaitingId를_사용해_findByIdAndMemberId를_호출하면_빈_Optional을_반환한다() {
        // given
        Member member = saveMember("Alice");

        // when
        Optional<ReserveWaiting> reserveWaiting = reserveWaitingRepository.findByIdAndMemberId(1L, member.getId());

        // then
        assertThat(reserveWaiting).isEmpty();
    }

    @Test
    void 예약_대기를_한_member로_잘못된_ReserveWaitingId를_사용해_findByIdAndMemberId를_호출하면_빈_Optional을_반환한다() {
        // given
        Member member = findMember("더미_유저");

        // when
        Optional<ReserveWaiting> reserveWaiting = reserveWaitingRepository.findByIdAndMemberId(2L, member.getId());

        // then
        assertThat(reserveWaiting).isEmpty();
    }

    @Test
    void 예약_대기를_한_member로_올바른_ReserveWaitingId를_사용해_findByIdAndMemberId를_호출하면_유효한_Optional을_반환한다() {
        // given
        Member member = findMember("더미_유저");

        // when
        Optional<ReserveWaiting> reserveWaiting = reserveWaitingRepository.findByIdAndMemberId(1L, member.getId());

        // then
        assertThat(reserveWaiting).isPresent();
        ReserveWaiting presentReserveWaiting = reserveWaiting.orElse(null);
        assertThat(presentReserveWaiting.getId()).isNotNull();
        assertThat(presentReserveWaiting.getMember()).isEqualTo(member);
        assertThat(presentReserveWaiting.getDate()).isNotNull();
        assertThat(presentReserveWaiting.getTime()).isNotNull();
        assertThat(presentReserveWaiting.getTheme()).isNotNull();
    }

    @Test
    void 유효한_ReserveWaitingId를_사용해_delete를_호출하면_해당_레코드가_삭제된다() {
        // given
        ReserveWaiting reserveWaiting = findReserveWaiting(1L);

        // when
        reserveWaitingRepository.deleteById(reserveWaiting.getId());
        List<ReserveWaiting> allReserveWaiting = reserveWaitingRepository.findAll();

        // then
        assertThat(allReserveWaiting).isEmpty();
    }

    @Test
    void 잘못된_ReserveWaitingId를_사용해_delete를_호출하면_조용히_넘어간다() {
        // when
        reserveWaitingRepository.deleteById(2L);
        List<ReserveWaiting> allReserveWaiting = reserveWaitingRepository.findAll();

        // then
        assertThat(allReserveWaiting).hasSize(1);
    }

    private ReserveWaiting findReserveWaiting(Long id) {
        return reserveWaitingRepository.findById(id).orElse(null);
    }

    private Time findTime(Long id) {
        return timeRepository.findById(id).orElse(null);
    }

    private Time saveTime() {
        return timeRepository.save(new Time(LocalTime.of(3, 0)));
    }

    private Theme findTheme(Long id) {
        return themeRepository.findById(id).orElse(null);
    }

    private Member findMember(String nickname) {
        return memberRepository.findByNickname(nickname).orElse(null);
    }

    private Member saveMember(String nickname) {
        return memberRepository.save(new Member(nickname, nickname.toLowerCase() + "@dummy.com", "dummy", "USER"));
    }
}
