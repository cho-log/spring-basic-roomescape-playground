package roomescape.domain.auth.service;

import auth.principal.LoginMember;
import org.springframework.stereotype.Service;
import roomescape.domain.auth.repository.AuthRepository;
import roomescape.global.exception.UnauthorizedException;

@Service
public class AuthService {

    private final AuthRepository authRepository;

    public AuthService(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public LoginMember login(String email, String password) {
        return authRepository.findByEmailAndPassword(email, password)
                .orElseThrow(UnauthorizedException::new);
    }
}
