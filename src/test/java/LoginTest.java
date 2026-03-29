import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.example.LoginUser;
import org.example.NewUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class LoginTest {

    private String testEmail;
    private String testName;
    private String testPassword;
    private String authBearer;

    @BeforeEach
    void setUp(){
        testEmail = (UUID.randomUUID()) + "@mail.ru";
        testName = "TestUser";
        testPassword = testName + "123456789";
        NewUser user = new NewUser(testEmail, testPassword, testName);
        authBearer = given()
                .contentType(ContentType.JSON)
                .log().all()
                .body(user)
                .when()
                .post("https://stellarburgers.education-services.ru/api/auth/register")
                .then()
                .log().ifError()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .extract()
                .path("accessToken");

    }

    @AfterEach
    void tearDown() {
        given()
                .header("Authorization", authBearer)
                .contentType(ContentType.JSON)
                .when()
                .delete("https://stellarburgers.education-services.ru/api/auth/user")
                .then()
                .statusCode(202);
    }

    @DisplayName("Авторизация пользователя")
    @Test
    void loginUser(){
        loginUserStep()
                .then()
                .log().ifError()
                .statusCode(200)
                .body("accessToken", notNullValue());
    }


    @DisplayName("Авторизация несуществующего пользователя")
    @Test
    void loginError(){
        testEmail = "123" + testEmail;
        testPassword = "4321" + testPassword;
        loginUserStep()
                .then()
                .log().ifError()
                .statusCode(401)
                .body("message", equalTo("email or password are incorrect"));
    }

    @Step("Авторизация пользователя")
    public Response loginUserStep(){
        LoginUser user = new LoginUser(testEmail, testPassword);
        return given()
                .contentType(ContentType.JSON)
                .log().all()
                .body(user)
                .when()
                .post("https://stellarburgers.education-services.ru/api/auth/login");
    }
}
