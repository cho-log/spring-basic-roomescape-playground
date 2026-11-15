package roomescape.global.data;

import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryDependsOnPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class SchemaInitializerDependency extends EntityManagerFactoryDependsOnPostProcessor {

    public SchemaInitializerDependency() {
        super(SchemaInitializer.class);
    }
}
