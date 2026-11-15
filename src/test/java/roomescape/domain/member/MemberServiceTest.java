package roomescape.domain.member;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.member.entity.Member;
import roomescape.domain.member.service.MemberService;
import roomescape.global.data.SchemaInitializer;
import roomescape.global.data.SchemaInitializerDependency;
import roomescape.global.data.TestDataLoader;
import roomescape.global.exception.ConflictException;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({MemberService.class, SchemaInitializer.class, SchemaInitializerDependency.class, TestDataLoader.class})
public class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    private final String name = "Alice";
    private final String email = "dummy@dummy.com";
    private final String password = "dummy";

    @Test
    void 이미_저장된_email로_createMember를_호출하면_409_예외를_던진다() {
        // data-test.sql
        Assertions.assertThrows(
                ConflictException.class,
                () -> memberService.createMember(name, "user@dummy.com", password)
        );
    }

    @Test
    void 이미_저장된_nickname으로_createMember를_호출하면_409_예외를_던진다() {
        // TestDataLoader: 더미_유저(user@dummy.com)
        ConflictException exception = Assertions.assertThrows(
                ConflictException.class,
                () -> memberService.createMember("더미_유저", email, password)
        );

        assertThat(exception.getMessage()).isEqualTo("이미 사용 중인 닉네임입니다.");
    }

    @Test
    void nickname과_email이_모두_중복이면_email_중복_예외를_먼저_던진다() {
        // TestDataLoader: 더미_유저(user@dummy.com)
        ConflictException exception = Assertions.assertThrows(
                ConflictException.class,
                () -> memberService.createMember("더미_유저", "user@dummy.com", password)
        );

        assertThat(exception.getMessage()).isEqualTo("이미 가입된 정보입니다.");
    }

    @Test
    void 기존_nickname과_일부만_같은_nickname으로는_createMember_메소드가_실행됨() {
        // when
        Member member = memberService.createMember("더미_유저2", email, password);

        // then
        assertThat(member.getId()).isNotNull();
        assertThat(member.getNickname()).isEqualTo("더미_유저2");
    }

    @Test
    void 정상적으로_createMember_메소드가_실행됨() {
        // when
        Member member = memberService.createMember(name, email, password);

        // then
        assertThat(member.getId()).isNotNull();
        assertThat(member.getNickname()).isEqualTo(name);
        assertThat(member.getEmail()).isEqualTo(email);
        assertThat(member.getPassword()).isEqualTo(password);
        assertThat(member.getRole()).isEqualTo("USER");
    }
}
