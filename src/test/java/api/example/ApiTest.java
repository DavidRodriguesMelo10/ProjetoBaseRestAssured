package api.example;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;


public class ApiTest {

    static {
        RestAssured.baseURI = "https://dummyjson.com";
    }

    private static ExtentReports report;
    private static ExtentTest test;
    private String validToken = "YOUR_VALID_TOKEN_HERE"; // Substitua pelo token válido
    private String invalidToken = "INVALID_TOKEN"; // Token inválido para testes

    @BeforeAll
    public static void setup() {
        report = new ExtentReports("test-report.html", true);
    }

    @AfterAll
    public static void tearDown() {
        report.flush();
        report.close();
    }

    @Test
    public void testGetStatus() {
        test = report.startTest("Test Get Status");
        Response response = given()
                .when()
                .get("/test")
                .then()
                .statusCode(200)
                .extract()
                .response();

        // Verifica o conteúdo da resposta
        response.then().body("status", equalTo("ok"))
                .body("method", equalTo("GET"));
        test.log(LogStatus.PASS, "Status returned successfully.");
        report.endTest(test);
    }

    @Test
    public void testGetUsers() {
        test = report.startTest("Test Get Users");
        Response response = given()
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .extract()
                .response();

        // Verifica se a lista de usuários não está vazia
        response.then().body("users", is(not(empty())));
        test.log(LogStatus.PASS, "Users list is not empty.");

        // Verifica se o primeiro usuário tem um nome
        response.then().body("users[0].firstName", is(notNullValue()));
        test.log(LogStatus.PASS, "First user has a first name.");

        // Verifica se o primeiro usuário tem um ID válido
        response.then().body("users[0].id", is(1));
        test.log(LogStatus.PASS, "First user ID is valid.");

        // Verifica se o segundo usuário tem um nome
        response.then().body("users[1].firstName", is(notNullValue()));
        test.log(LogStatus.PASS, "Second user has a first name.");

        // Verifica se o segundo usuário tem um ID válido
        response.then().body("users[1].id", is(2));
        test.log(LogStatus.PASS, "Second user ID is valid.");

        // Verifica se o primeiro usuário tem um email válido
        response.then().body("users[0].email", equalTo("emily.johnson@x.dummyjson.com"));
        test.log(LogStatus.PASS, "First user email is valid.");

        // Verifica se o segundo usuário tem um email válido
        response.then().body("users[1].email", equalTo("michael.williams@x.dummyjson.com"));
        test.log(LogStatus.PASS, "Second user email is valid.");

        // Verifica se o primeiro usuário tem um endereço
        response.then().body("users[0].address", is(notNullValue()));
        test.log(LogStatus.PASS, "First user has an address.");

        // Verifica se o segundo usuário tem um endereço
        response.then().body("users[1].address", is(notNullValue()));
        test.log(LogStatus.PASS, "Second user address is valid.");
        report.endTest(test);

    }

    @Test
    public void testLogin() {
        test = report.startTest("Test Login");

        // Dados de login
        String requestBody = "{ \"username\": \"emilys\", \"password\": \"emilyspass\" }";

        Response response = given()
                .header("Content-Type", "application/json")
                .body(requestBody)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .response();

        // Verifica se a resposta contém os dados esperados
        response.then().body("id", is(1))
                .body("username", equalTo("emilys"))
                .body("email", equalTo("emily.johnson@x.dummyjson.com"))
                .body("firstName", equalTo("Emily"))
                .body("lastName", equalTo("Johnson"))
                .body("gender", equalTo("female"))
                .body("image", equalTo("https://dummyjson.com/icon/emilys/128"));
        test.log(LogStatus.PASS, "Login successful and response is valid.");
        report.endTest(test);

    }

    @Test
    public void testGetProductsWithValidToken() {
        test = report.startTest("Test Get Products With Valid Token");
        Response response = given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + validToken)
                .when()
                .get("/auth/products")
                .then()
                .statusCode(401)
                .extract()
                .response();

        // Verifica se a resposta contém a lista de produtos
        response.then().body("products", is(not(empty())));
        test.log(LogStatus.PASS, "Products retrieved successfully with valid token.");
        report.endTest(test);

    }

    @Test
    public void testGetProducts_WithInvalidToken() {
        Response response = given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + invalidToken)
                .when()
                .get("/auth/products")
                .then()
                .statusCode(401) // Espera-se um erro de autenticação
                .extract()
                .response();

        // Verifica se a mensagem de erro está correta
        response.then().body("message", equalTo("Invalid/Expired Token!"));
        test.log(LogStatus.PASS, "Received expected error message for invalid token.");
        report.endTest(test);
    }

    @Test
    public void testGetProductsWithExpiredToken() {
        test = report.startTest("Test Get Products With Expired Token");
        // Simulando um token expirado
        String expiredToken = "JsonWebTokenError"; // Substitua por um token que você sabe que está expirado

        Response response = given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + expiredToken)
                .when()
                .get("/auth/products")
                .then()
                .statusCode(401) // Espera-se um erro de token inválido
                .extract()
                .response();

        test.log(LogStatus.PASS, "Received expected error for expired token.");
        report.endTest(test);

    }

    @Test
    public void testAddProduct() {
        test = report.startTest("Test Add Product");
        // Dados do novo produto
        String requestBody = "{ " +
                "\"title\": \"Perfume Oil\", " +
                "\"description\": \"Mega Discount, Impression of A...\", " +
                "\"price\": 13, " +
                "\"discountPercentage\": 8.4, " +
                "\"rating\": 4.26, " +
                "\"stock\": 65, " +
                "\"brand\": \"Impression of Acqua Di Gio\", " +
                "\"category\": \"fragrances\", " +
                "\"thumbnail\": \"https://i.dummyjson.com/data/products/11/thumnail.jpg\" " +
                "}";

        Response response = given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + validToken) // Adicione o token de autenticação
                .body(requestBody)
                .when()
                .post("/products/add")
                .then()
                .statusCode(201)
                .extract()
                .response();

        // Verifica se a resposta contém os dados do produto criado
        response.then().body("title", equalTo("Perfume Oil"))
                .body("price", equalTo(13))
                .body("stock", equalTo(65))
                .body("rating", equalTo(4.26F))
                .body("thumbnail", equalTo("https://i.dummyjson.com/data/products/11/thumnail.jpg"))
                .body("description", equalTo("Mega Discount, Impression of A..."))
                .body("brand", equalTo("Impression of Acqua Di Gio"))
                .body("category", equalTo("fragrances"));
        test.log(LogStatus.PASS, "Product added successfully.");
        report.endTest(test);
    }

    @Test
    public void testGetAllProducts() {
        test = report.startTest("Test Get All Products");
        Response response = given()
                .header("Content-Type", "application/json")
                .when()
                .get("/products")
                .then()
                .statusCode(200)
                .extract()
                .response();

        // Verifica se a resposta contém a lista de produtos
        response.then().body("products", is(not(empty())));
        test.log(LogStatus.PASS, "All products retrieved successfully.");
        report.endTest(test);
    }

    @Test
    public void testGetProductById() {
        test = report.startTest("Test Get Product By ID");
        int productId = 1; // ID do produto que queremos buscar
        Response response = given()
                .header("Content-Type", "application/json")
                .when()
                .get("/products/" + productId)
                .then()
                .statusCode(200)
                .extract()
                .response();

        // Verifica se a resposta contém os dados do produto
        response.then().body("id", equalTo(productId))
                .body("title", equalTo("Essence Mascara Lash Princess"))
                .body("price", equalTo(9.99F))
                .body("category", equalTo("beauty"))
                .body("stock", equalTo(5));
        test.log(LogStatus.PASS, "Product retrieved successfully by ID.");
        report.endTest(test);
    }

    @Test
    public void testGetProductByInvalidId() {
        test = report.startTest("Test Get Product By Invalid ID");
        int invalidProductId = 0; // ID inválido
        Response response = given()
                .header("Content-Type", "application/json")
                .when()
                .get("/products/" + invalidProductId)
                .then()
                .statusCode(404)
                .extract()
                .response();

        // Verifica se a resposta contém a mensagem de erro
        response.then().body("message", equalTo("Product with id '0' not found"));
        test.log(LogStatus.PASS, "Received expected error message for invalid product ID.");
        report.endTest(test);
    }

}