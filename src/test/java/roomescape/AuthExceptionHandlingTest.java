package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import auth.JwtTokenProvider;
import org.springframework.test.context.ActiveProfiles;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

import java.util.HashMap;
import java.util.Map;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AuthExceptionHandlingTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Value("${roomescape.auth.jwt.secret}")
    private String secretKey;

    @Test
    void 잘못된_비밀번호로_로그인하면_401을_응답한다() {
        Map<String, String> params = new HashMap<>();
        params.put("email", "admin@email.com");
        params.put("password", "wrong-password");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/login")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    void 토큰_쿠키_없이_로그인_정보를_조회하면_401을_응답한다() {
        RestAssured.given().log().all()
                .when().get("/login/check")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    void 잘못된_토큰으로_관리자_페이지를_요청하면_401을_응답한다() {
        RestAssured.given().log().all()
                .cookie("token", "invalid-token")
                .when().get("/admin")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    void 토큰의_회원이_존재하지_않으면_401을_응답한다() {
        Member nonexistentMember = new Member(
                9999L,
                "존재하지 않는 회원",
                "none@email.com",
                Role.USER
        );

        String token = jwtTokenProvider.createToken(nonexistentMember.getId(), nonexistentMember.getRole().name());

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    void 만료된_토큰으로_로그인_정보를_조회하면_401을_응답한다() {
        JwtTokenProvider expiredTokenProvider = new JwtTokenProvider(secretKey, -1000L);

        Member member = new Member(
                1L,
                "어드민",
                "admin@email.com",
                Role.ADMIN
        );

        String expiredToken = expiredTokenProvider.createToken(member.getId(), member.getRole().name());

        RestAssured.given().log().all()
                .cookie("token", expiredToken)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    void 비로그인_사용자는_관리_API를_사용할_수_없다() {
        Map<String, String> time = new HashMap<>();
        time.put("value", "22:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(time)
                .when().post("/times")
                .then().log().all()
                .statusCode(401);

        RestAssured.given().log().all()
                .when().delete("/themes/1")
                .then().log().all()
                .statusCode(401);

        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(401);
    }
}
