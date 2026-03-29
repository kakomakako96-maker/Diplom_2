import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.example.LoginUser;
import org.example.ModifyUser;
import org.example.NewUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class ModifyLoginTest {

    private String testEmail;
    private String testName;
    private String newTestEmail;
    private String newTestName;
    private String testPassword;
    private String authBearer;

    @BeforeEach
    void setUp() {
        testEmail = (UUID.randomUUID()) + "@mail.ru";
        testName = "TestUser";
        testPassword = testName + "123456789";
        newTestEmail = (UUID.randomUUID()) + "@mail.ru";
        newTestName = "NewTestUser";
        NewUser user = new NewUser(testEmail, testPassword, testName);
        given()
                .contentType(ContentType.JSON)
                .body(user)
                .when()
                .post("https://stellarburgers.education-services.ru/api/auth/register")
                .then()
                .log().ifError()
                .statusCode(200)
                .body("accessToken", notNullValue());
    }

    @AfterEach
    void tearDown() {
        if (authBearer != null && !authBearer.isEmpty()) {
            given()
                    .header("Authorization", authBearer)
                    .contentType(ContentType.JSON)
                    .when()
                    .delete("https://stellarburgers.education-services.ru/api/auth/user")
                    .then()
                    .statusCode(202);
        }
    }

    private static Stream<Arguments> modify() {
        String newTestEmail = (UUID.randomUUID()) + "@test-mail.ru";
        String newTestName = String.valueOf((UUID.randomUUID()));
        return Stream.of(
                Arguments.of(null, newTestName),
                Arguments.of(newTestEmail, null)
        );
    }

    @DisplayName("Изменение данных авторизированного пользователя")
    @ParameterizedTest
    @MethodSource("modify")
    void modifyAuthUser(String email, String name) {
        authBearer = loginUserStep()
                .then()
                .log().ifError()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .extract()
                .path("accessToken");

        modifyUser(authBearer, email, name)
                .then()
                .log().ifError()
                .statusCode(200);

        given()
                .header("Authorization", authBearer)
                .log().all()
                .contentType(ContentType.JSON)
                .when()
                .get("https://stellarburgers.education-services.ru/api/auth/user")
                .then()
                .log().body()
                .statusCode(200);
    }

    @DisplayName("Изменение данных не авторизированного пользователя")
    @Test
    void modifyAuthUserNotAuth() {

        modifyUserNotAuth()
                .then()
                .log().ifError()
                .statusCode(401);
    }

    @Step("Авторизация пользователя")
    public Response loginUserStep() {
        LoginUser user = new LoginUser(testEmail, testPassword);
        return given()
                .contentType(ContentType.JSON)
                .body(user)
                .when()
                .post("https://stellarburgers.education-services.ru/api/auth/login");
    }

    @Step("Изменение данных пользователя c авторизацией")
    public Response modifyUser(String authBearer, String email, String name) {
        ModifyUser user;
        if (email == null) {
            String emailNull  = testEmail;
            user = new ModifyUser(emailNull, name);
        } else {
            user = new ModifyUser(email, name);
        }
        return given()
                .header("Authorization", authBearer)
                .log().all()
                .contentType(ContentType.JSON)
                .body(user)
                .when()
                .patch("https://stellarburgers.education-services.ru/api/auth/user");

    }

    @Step("Изменение данных пользователя без авторизации")
    public Response modifyUserNotAuth() {
        ModifyUser user = new ModifyUser(newTestEmail, newTestName);
        return given()
                .contentType(ContentType.JSON)
                .log().all()
                .body(user)
                .when()
                .patch("https://stellarburgers.education-services.ru/api/auth/user");
    }

}
