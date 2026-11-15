package roomescape.member.domain;

import jakarta.persistence.*;
import roomescape.exception.member.InvalidMemberException;

@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Column(unique = true)
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    public Member(Long id, String name, String email, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public Member(String name, String email, String password, Role role) {
        validateRequiredFields(name, email, password);

        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Member() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    private void validateRequiredFields(
            String name,
            String email,
            String password
    ) {
        if (name == null || name.isBlank()
                || email == null || email.isBlank()
                || password == null || password.isBlank()) {
            throw new InvalidMemberException("회원 정보를 모두 입력해야 합니다.");
        }
    }
}
