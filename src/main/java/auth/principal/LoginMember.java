package auth.principal;

public record LoginMember (
        Long id,
        String name,
        String email,
        String role
) {

    public LoginMember(Long id, String name, String email, String role) {
        validateFields(id, name, email, role);
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public boolean isAdmin() {
        return this.role != null && this.role.equals("ADMIN");
    }

    private void validateFields(Long id, String name, String email, String role) {
        if (id == null) {
            throw new IllegalArgumentException("LoginMember를 생성하기 위해 ID는 필수 필드입니다.");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("LoginMember를 생성하기 위해 name은 필수 필드입니다.");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("LoginMember를 생성하기 위해 email은 필수 필드입니다.");
        }

        if (!email.contains("@") || !email.endsWith(".com")) {
            throw new IllegalArgumentException("LoginMember.email 필드의 정규식이 올바르지 않습니다.");
        }

        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("LoginMember를 생성하기 위해 role은 필수 필드입니다.");
        }

        if (!role.equals("USER") && !role.equals("ADMIN")) {
            throw new IllegalArgumentException("잘못된 권한입니다.");
        }
    }
}
