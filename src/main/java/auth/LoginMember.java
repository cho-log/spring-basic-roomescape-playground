package auth;

public record LoginMember(
        Long id,
        String name,
        String role
) {

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
