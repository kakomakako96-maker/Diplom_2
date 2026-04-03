import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;


public class NewUserTest {

    private static String testEmail;
    private static String testPassword;
    private static String testName;
    private String authBearer;

    @BeforeEach
    public void setUp() {
        testEmail = (UUID.randomUUID()) + "@mail.ru";
        testName = "TestUser";
        testPassword = testName + "123456789";
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

    @DisplayName("Создание пользователя")
    @Test
    void createNewUser() {
        authBearer = createUserStep()
                .then()
                .log().ifError()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .extract()
                .path("accessToken");
    }

    @DisplayName("Создание существующего пользователя")
    @Test
    void createDoubleUser() {
        authBearer = createUserStep()
                .then()
                .log().ifError()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .extract().path("accessToken");

        createUserStep()
                .then()
                .log().ifError()
                .statusCode(403)
                .body("message", equalTo("User already exists"));
    }

    private static Stream<Arguments> fieldUser() {
        testEmail = (UUID.randomUUID()) + "@mail.ru";
        testPassword = testName + "123456789";
        testName = "TestUser";
        return Stream.of(
                Arguments.of(testEmail, null, testName),
                Arguments.of(null, testPassword, testName),
                Arguments.of(testEmail, testPassword, null)
        );
    }

    @DisplayName("Создание пользователя с одним обязательным полем")
    @ParameterizedTest
    @MethodSource("fieldUser")
    void createUserOneField(String email, String password, String name) {
        createUserOneFieldStep(email, password, name)
                .then()
                .log().ifError()
                .statusCode(403)
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Step("Создание пользователя")
    public Response createUserStep() {
        NewUser user = new NewUser(testEmail, testPassword, testName);
        return given()
                .contentType(ContentType.JSON)
                .log().all()
                .body(user)
                .when()
                .post("https://stellarburgers.education-services.ru/api/auth/register");
    }

    @Step("Создание пользователя с одним полем обязательным полем")
    public Response createUserOneFieldStep(String email, String password, String name) {
        NewUser newUserOnneField = new NewUser(email, password, name);
        return given()
                .contentType(ContentType.JSON)
                .log().all()
                .body(newUserOnneField)
                .when()
                .post("https://stellarburgers.education-services.ru/api/auth/register");
    }

}