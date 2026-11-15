package roomescape.global.data;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

@Component
public class SchemaInitializer implements InitializingBean {

    private final JdbcTemplate jdbcTemplate;

    public SchemaInitializer(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    @Transactional
    public void afterPropertiesSet() {
        jdbcTemplate.execute("""
create table member (
    id          bigint          not null    auto_increment,
    nickname    varchar(255)    not null    unique,
    email       varchar(255)    not null    unique,
    password    varchar(255)    not null,
    role        varchar(255)    not null,
    
    primary key (id)
)
""");
        jdbcTemplate.execute("""
create table theme (
    id          bigint          not null    auto_increment,
    name        varchar(255)    not null    unique,
    description varchar(255)    not null,
    
    primary key (id)
)
""");
        jdbcTemplate.execute("""
create table time (
    id          bigint  not null    auto_increment,
    time_value  time    not null    unique,
    
    primary key (id)
)
""");
        jdbcTemplate.execute("""
create table reservation (
    id          bigint  not null    auto_increment,
    date        date    not null,
    member_id   bigint  not null,
    time_id     bigint  not null,
    theme_id    bigint  not null,
    
    primary key (id),
    foreign key (member_id) references member (id),
    constraint fk_reservation_time foreign key (time_id) references time (id),
    constraint fk_reservation_theme foreign key (theme_id) references theme (id),
    constraint uk_reservation_Date_time_theme unique (date, time_id, theme_id)
)
""");
        jdbcTemplate.execute("""
create table reserve_waiting (
    id          bigint  not null    auto_increment,
    member_id   bigint  not null,
    date        date    not null,
    time_id     bigint  not null,
    theme_id    bigint  not null,
    
    primary key (id),
    foreign key (member_id) references member (id),
    constraint fk_reserve_waiting_time foreign key (time_id) references time (id),
    constraint fk_reserve_waiting_theme foreign key (theme_id) references theme (id),
    constraint uk_reserve_waiting_member_date_time_theme unique (date, theme_id, time_id, member_id)
)
""");
    }
}
