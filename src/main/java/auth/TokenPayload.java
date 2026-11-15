package auth;

import java.time.Instant;

public record TokenPayload(
        Long memberId,
        String name,
        String email,
        String role,
        String tokenId,
        Instant expiresAt
) {
}
