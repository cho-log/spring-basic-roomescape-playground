package roomescape.domain.auth.repository;

import auth.principal.LoginMember;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import roomescape.domain.member.entity.Member;

import java.util.Optional;

public interface AuthRepository extends Repository<Member, Long> {

    @Query("""
select new auth.principal.LoginMember(
    m.id,
    m.nickname,
    m.email,
    m.role
)
from Member m
where m.email = :email
    and m.password = :password
""")
    Optional<LoginMember> findByEmailAndPassword(
            @Param("email") String email,
            @Param("password") String password
    );
}
