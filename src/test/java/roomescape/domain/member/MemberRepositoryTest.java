package roomescape.domain.member;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import roomescape.domain.member.entity.Member;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.global.data.SchemaInitializer;
import roomescape.global.data.SchemaInitializerDependency;
import roomescape.global.data.TestDataLoader;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({SchemaInitializer.class, SchemaInitializerDependency.class, TestDataLoader.class})
public class MemberRepositoryTest {

    private final String name = "Alice";
    private final String conflictNickname = "더미_유저";
    private final String email = "test@test.com";
    private final String password = "test";
    private final String role = "USER";

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void save를_호출하면_ID가_있는_객체를_반환한다() {
        // given
        Member member = new Member(name, email, password, role);

        // when
        Member savedMember = memberRepository.save(member);

        // then
        assertThat(savedMember).isSameAs(member);
        assertThat(savedMember.getId()).isNotNull();
    }

    @Test
    void 중복된_email로_저장하면_예외가_발생한다() {
        // data-test.sql
        Assertions.assertThrows(
                DataIntegrityViolationException.class,
                () -> memberRepository.save(new Member(name, "user@dummy.com", password, role))
        );
    }

    @Test
    void 중복된_nickname으로_저장하면_예외가_발생한다() {
        // data-test.sql
        Assertions.assertThrows(
                DataIntegrityViolationException.class,
                () -> memberRepository.save(new Member(conflictNickname, email, password, role))
        );
    }

    @Test
    void 이미_저장된_email로_existsByEmail을_호출하면_true를_반환한다() {
        // data-test.sql
        boolean isExistsEmail = memberRepository.existsByEmail("admin@dummy.com");

        assertThat(isExistsEmail).isTrue();
    }

    @Test
    void 저장된_적_없는_email로_existsByEmail을_호출하면_false를_반환한다() {
        // data-test.sql
        boolean isExistsEmail = memberRepository.existsByEmail(email);

        assertThat(isExistsEmail).isFalse();
    }

    @Test
    void 이미_저장된_nickname으로_existsByNickname을_호출하면_true를_반환한다() {
        // data-test.sql
        boolean isExistsEmail = memberRepository.existsByNickname(conflictNickname);

        assertThat(isExistsEmail).isTrue();
    }

    @Test
    void 저장된_적_없는_nickname으로_existsByNickname을_호출하면_false를_반환한다() {
        // data-test.sql
        boolean isExistsNickname = memberRepository.existsByNickname(name);

        assertThat(isExistsNickname).isFalse();
    }

    @Test
    void 저장된_id로_findByIdForUpdate를_호출하면_일치하는_Member를_반환한다() {
        // data-test.sql
        Optional<Member> foundMember = memberRepository.findByIdForUpdate(2L);

        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getNickname()).isEqualTo(conflictNickname);
    }

    @Test
    void 저장된_적_없는_id로_findByIdForUpdate를_호출하면_빈_Optional을_반환한다() {
        // data-test.sql
        Optional<Member> foundMember = memberRepository.findByIdForUpdate(-1L);

        assertThat(foundMember).isEmpty();
    }

    @Test
    void 저장된_nickname으로_findByNicknameForUpdate를_호출하면_일치하는_Member를_반환한다() {
        // data-test.sql
        Optional<Member> foundMember = memberRepository.findByNicknameForUpdate(conflictNickname);

        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getId()).isEqualTo(2L);
    }

    @Test
    void 저장된_적_없는_nickname으로_findByNicknameForUpdate를_호출하면_빈_Optional을_반환한다() {
        // data-test.sql
        Optional<Member> foundMember = memberRepository.findByNicknameForUpdate(name);

        assertThat(foundMember).isEmpty();
    }

    @Test
    void ADMIN계정_생성이_정상적으로_처리된_경우() {
        // given
        Member builtAdmin = new Member(name, email, password, "ADMIN");

        // when
        Member savedAdmin = memberRepository.save(builtAdmin);

        // then
        assertThat(savedAdmin.getId()).isNotNull();
        assertThat(savedAdmin.getNickname()).isEqualTo(name);
        assertThat(savedAdmin.getEmail()).isEqualTo(email);
        assertThat(savedAdmin.getPassword()).isEqualTo(password);
        assertThat(savedAdmin.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void USER계정_생성이_정상적으로_처리된_경우() {
        // given
        Member builtAdmin = new Member(name, email, password, role);

        // when
        Member savedAdmin = memberRepository.save(builtAdmin);

        // then
        assertThat(savedAdmin.getId()).isNotNull();
        assertThat(savedAdmin.getNickname()).isEqualTo(name);
        assertThat(savedAdmin.getEmail()).isEqualTo(email);
        assertThat(savedAdmin.getPassword()).isEqualTo(password);
        assertThat(savedAdmin.getRole()).isEqualTo(role);
    }
}
