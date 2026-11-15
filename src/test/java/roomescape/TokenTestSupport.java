package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

class TokenTestSupport {

    static String createToken(String email, String password) {
        Map<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);

        ExtractableResponse<Response> response =
                RestAssured.given().log().all()
                        .contentType(ContentType.JSON)
                        .body(params)
                        .when().post("/login")
                        .then().log().all()
                        .statusCode(200)
                        .extract();

        return response.headers()
                .get("Set-Cookie")
                .getValue()
                .split(";")[0]
                .split("=")[1];
    }
}
