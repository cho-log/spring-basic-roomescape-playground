INSERT INTO member(name, email, password, role) VALUES ('어드민', 'admin@email.com', 'password', 'ADMIN');
INSERT INTO member(name, email, password, role) VALUES ('브라운', 'brown@email.com', 'password', 'USER');
INSERT INTO time(time_value, deleted) VALUES (TIME '10:00', false), (TIME '12:00', false), (TIME '14:00', false);
INSERT INTO theme(name, description, deleted) VALUES ('테마1', '설명1', false), ('테마2', '설명2', false), ('테마3', '설명3', false);

INSERT INTO reservation (member_id, name, date, time_id, theme_id)
VALUES (1, '', DATE '2024-03-01', 1, 1),
       (1, '', DATE '2024-03-01', 2, 2),
       (1, '', DATE '2024-03-01', 3, 3);

INSERT INTO reservation (name, date, time_id, theme_id)
VALUES ('브라운', DATE '2024-03-01', 1, 2);