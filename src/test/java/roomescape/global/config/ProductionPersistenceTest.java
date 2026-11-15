package roomescape.global.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import roomescape.RoomescapeApplication;
import roomescape.member.MemberRepository;
import roomescape.theme.Theme;
import roomescape.theme.ThemeRepository;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ProductionPersistenceTest {

    @TempDir
    Path directory;

    @Test
    void 운영_DB는_재시작해도_데이터를_유지한다() {
        // given
        Long themeId;
        try (ConfigurableApplicationContext context = startApplication()) {
            ThemeRepository themes = context.getBean(ThemeRepository.class);
            themeId = themes.save(new Theme("유지할 테마", "재시작 검증")).getId();
        }

        // when
        try (ConfigurableApplicationContext context = startApplication()) {
            ThemeRepository themes = context.getBean(ThemeRepository.class);
            Theme savedTheme = themes.findById(themeId).orElseThrow();

            // then
            assertThat(directory.resolve("database.mv.db")).exists();
            assertThat(savedTheme.getName()).isEqualTo("유지할 테마");
            assertThat(context.getBean(MemberRepository.class).count()).isEqualTo(2L);
        }
    }

    private ConfigurableApplicationContext startApplication() {
        return new SpringApplicationBuilder(RoomescapeApplication.class)
                .web(WebApplicationType.NONE)
                .run("--spring.profiles.active=prod",
                        "--ROOMESCAPE_DB_PATH=" + directory.resolve("database"));
    }
}
