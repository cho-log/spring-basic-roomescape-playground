package roomescape.domain.theme;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.theme.repository.ThemeRepository;
import roomescape.global.data.SchemaInitializer;
import roomescape.global.data.SchemaInitializerDependency;
import roomescape.global.data.TestDataLoader;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({SchemaInitializer.class, SchemaInitializerDependency.class, TestDataLoader.class})
public class ThemeRepositoryTest {

    private final String name = "Dummy";
    private final String description = "it is Dummy for test";

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    void save를_호출하면_ID가_있는_객체를_반환한다() {
        // given
        Theme theme = new Theme(name, description);

        // when
        Theme savedTheme = themeRepository.save(theme);

        // then
        assertThat(theme).isSameAs(savedTheme);
        assertThat(savedTheme.getId()).isNotNull();

        assertThat(savedTheme.getName()).isEqualTo(name);
        assertThat(savedTheme.getDescription()).isEqualTo(description);
    }

    @Test
    void findAll을_호출하면_저장된_모든_테마를_반환한다() {
        // given
        themeRepository.save(new Theme("테마A", "테마A입니다."));
        themeRepository.save(new Theme("테마B", "테마B입니다."));

        // when & then
        assertThat(themeRepository.findAll()).hasSize(3);
    }

    @Test
    void deleteById를_호출하면_해당_테마가_조회에서_제외된다() {
        // given
        Theme theme = new Theme(name, description);
        themeRepository.save(theme);

        // when
        themeRepository.deleteById(theme.getId());

        // then
        assertThat(themeRepository.findAll()).hasSize(1);
    }
}
