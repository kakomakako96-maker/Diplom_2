import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.example.NewOrder;
import org.example.NewUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class CheckOrderTest {

    private String testEmail;
    private String testName;
    private String testPassword;
    private List<String> ingredients = new ArrayList<>();
    private String refreshToken;
    private String authBearer;


    @BeforeEach
    void setUp() {
        testEmail = (UUID.randomUUID()) + "@mail.ru";
        testName = "TestUser";
        testPassword = testName + "123456789";
        NewUser user = new NewUser(testEmail, testPassword, testName);
        Response response = given()
                .contentType(ContentType.JSON)
                .body(user)
                .when()
                .post("https://stellarburgers.education-services.ru/api/auth/register")
                .then()
                .log().ifError()
                .statusCode(200)
                .extract().response();

        refreshToken = response.path("refreshToken");
        authBearer = response.path("accessToken");
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

    @DisplayName("Получение заказа авторизированного пользователя")
    @Test
    void checkOrderAuthUser() {

        ingredients.add(getIngredient());
        ingredients.add(getIngredient());

        createOrderStep(ingredients)
                .then()
                .log().all()
                .statusCode(200);

        getOrder()
                .then()
                .log().all()
                .statusCode(200);
    }

    @DisplayName("Получение заказа не авторизированного пользователя")
    @Test
    void checkOrderNotAuthUser() {
        logout()
                .then()
                .log().all()
                .statusCode(200);

        getOrderNotUser()
                .then()
                .log().all()
                .statusCode(401)
                .body("message", equalTo("You should be authorised"));
    }

    @Step("Вызов ингредиента")
    public String getIngredient() {
        List<String> ingredientIds = given()
                .contentType(ContentType.JSON)
                .when()
                .get("https://stellarburgers.education-services.ru/api/ingredients")
                .then()
                .extract()
                .path("data._id");

        Random random = new Random();
        return ingredientIds.get(random.nextInt(ingredientIds.size()));
    }

    @Step("Создание заказа")
    public Response createOrderStep(List<String> ingredients) {
        NewOrder order = new NewOrder(ingredients);
        return given()
                .contentType(ContentType.JSON)
                .body(order)
                .when()
                .post("https://stellarburgers.education-services.ru/api/orders");
    }

    @Step("Выход из аккаунта")
    public Response logout() {
        return given()
                .contentType(ContentType.JSON)
                .body("{\"token\": \"" + refreshToken + "\"}")
                .when()
                .post("https://stellarburgers.education-services.ru/api/auth/logout");
    }

    @Step("Получение заказа конкретного пользователя")
    public Response getOrder() {
        return given()
                .header("Authorization", authBearer)
                .contentType(ContentType.JSON)
                .when()
                .get("https://stellarburgers.education-services.ru/api/orders");
    }

    @Step("Получение заказа конкретного пользователя")
    public Response getOrderNotUser() {
        return given()
                .contentType(ContentType.JSON)
                .when()
                .get("https://stellarburgers.education-services.ru/api/orders");
    }

}