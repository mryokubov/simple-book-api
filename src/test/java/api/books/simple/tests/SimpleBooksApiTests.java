package api.books.simple.tests;

import static io.restassured.RestAssured.*;
import static org.hamcrest.CoreMatchers.*;
import api.books.simple.pojo.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class SimpleBooksApiTests {


    private static final String BASE_URL = "https://simple-books-api.glitch.me";
    private static final String ACCESS_TOKEN = "2272d1a328d942aab2f10d443e99fdc78a5d6edd878dc6512fd440258fc8e0ce";

    @BeforeClass
    public static void setUp(){
        //setting up the base url for rest assured before it makes a
        //request to any end point
        RestAssured.baseURI = BASE_URL;
    }


    @Test
    public void getApiStatusTest(){
        given()
                .when()
                .get("/status")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("OK"));
    }

    @Test
    public void getListOfBooksTest(){
        given()
                .when()
                .get("/books")
                .then()
                .statusCode(200) //verify status is 200
                .body("", Matchers.instanceOf(List.class))
                .contentType(ContentType.JSON)  //verify response was in json
                .body("size()", equalTo(6)); //verify number of resource was 6
    }

    @Test
    public void getListOfBooksVerifyEachTest(){
        Response response = given()
                .when()
                .get("/books")
                .then()
                .statusCode(200) //verify status is 200
                .extract()
                .response();

        BookResponse[] bookObjects = response.as(BookResponse[].class);

        for(BookResponse obj : bookObjects){
            Assert.assertTrue(obj.getId() != null);
        }
    }

    @Test
    public void getListOfBooksWithTypeQueryParamTest(){
        given()
                .queryParam("type", "fiction")
                .when()
                .get("/books")
                .then()
                .statusCode(200)
                .body("", Matchers.instanceOf(List.class))
                .contentType(ContentType.JSON)
                .body("size()", equalTo(4));
    }

    @Test
    public void getListOfBooksWithLimitQueryParamTest(){
        given()
                .queryParam("limit", 2)
                .when()
                .get("/books")
                .then()
                .statusCode(200)
                .body("", Matchers.instanceOf(List.class))
                .contentType(ContentType.JSON)
                .body("size()", equalTo(2));
    }

    @Test
    public void getSingleBookByIdTest(){
        given()
                .pathParam("bookId", 2)
                .when()
                .get("/books/{bookId}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(2));
    }

    @Test
    public void getSingleBookByIdValidateAllFieldsJSONPathTest(){
        Response response = given()
                .pathParam("bookId", 1)
                .when()
                .get("/books/{bookId}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .response();

        int id = response.jsonPath().getInt("id");
        String name = response.jsonPath().getString("name");
        String author = response.jsonPath().getString("author");
        String isbn = response.jsonPath().getString("isbn");
        String type = response.jsonPath().getString("type");
        double price = response.jsonPath().getDouble("price");
        int currentStock = response.jsonPath().getInt("current-stock");
        boolean available = response.jsonPath().getBoolean("available");

        Assert.assertEquals(1, id);
        Assert.assertEquals("The Russian", name);
        Assert.assertEquals("James Patterson and James O. Born", author);
        Assert.assertEquals("1780899475", isbn);
        Assert.assertEquals("fiction", type);
        Assert.assertEquals(12.98, price, 0.01);
        Assert.assertEquals(12, currentStock);
        Assert.assertEquals(true, available);

    }

    @Test
    public void getSingleBookByIdValidateAllFieldsPojoTest(){
        Response response = given()
                .pathParam("bookId", 1)
                .when()
                .get("/books/{bookId}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .response();

        BookDetailsResponse bookObj = response.as(BookDetailsResponse.class);

        Assert.assertEquals(1, bookObj.getId());
        Assert.assertEquals("The Russian", bookObj.getName());
        Assert.assertEquals("James Patterson and James O. Born", bookObj.getAuthor());
        Assert.assertEquals("1780899475", bookObj.getIsbn());
        Assert.assertEquals("fiction", bookObj.getType());
        Assert.assertEquals(12.98, bookObj.getPrice(), 0.01);
        Assert.assertEquals(12, bookObj.getCurrentStock());
        Assert.assertEquals(true, bookObj.isAvailable());
    }


    @Test
    public void getSingleBookByIdNegativeTest(){
        given()
                .pathParam("bookId", 20)
                .when()
                .get("/books/{bookId}")
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("error", equalTo("No book with id 20"));
    }

    @Test
    public void postSubmitBookOrderTest(){
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + ACCESS_TOKEN)
                .body(new SubmitOrderRequest(1, "Kevin Lee"))
                .post("/orders")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("created", equalTo(true))
                .body("orderId", notNullValue());
    }

    @Test
    public void postSubmitBookOrderBadTest(){
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + ACCESS_TOKEN)
                .body("{\n" +
                        "    \"bookId\": 1,\n" +
                        "    \"customerName\": \"Mike Lee\"\n" +
                        "}")
                .post("/orders")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("created", equalTo(true))
                .body("orderId", notNullValue());
    }

    @Test
    public void postSubmitBookOrderWithNoAccessTokenTest(){
        given()
                .contentType(ContentType.JSON)
                .body("{\n" +
                        "    \"bookId\": 1,\n" +
                        "    \"customerName\": \"Mike Lee\"\n" +
                        "}")
                .post("/orders")
                .then()
                .statusCode(401)
                .contentType(ContentType.JSON)
                .body("error", equalTo("Missing Authorization header."));
    }

    @Test
    public void postSubmitBookOrderWithInvalidTokenTokenTest(){
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer 2272d1a328d942aab2f10d443e99fdc78a5d6edd878dc6512fd440258fc8e0ck")
                .body("{\n" +
                        "    \"bookId\": 1,\n" +
                        "    \"customerName\": \"Mike Lee\"\n" +
                        "}")
                .post("/orders")
                .then()
                .statusCode(401)
                .contentType(ContentType.JSON)
                .body("error", equalTo("Invalid bearer token."));
    }

    @Test
    public void getAllOrdersTest(){
        given()
                .header("Authorization", "Bearer " + ACCESS_TOKEN)
                .get("/orders")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("", Matchers.instanceOf(List.class));
    }

    @Test
    public void getSingleOrderTest(){

        String orderId = placeOrderAndGetId();

        given()
                .pathParam("orderId", orderId)
                .header("Authorization", "Bearer " + ACCESS_TOKEN)
                .get("/orders/{orderId}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(orderId));
    }


    @Test
    public void deleteOrderTest(){

        String orderId = placeOrderAndGetId();

        given()
                .pathParam("orderId", orderId)
                .header("Authorization", "Bearer " + ACCESS_TOKEN)
                .delete("/orders/{orderId}")
                .then()
                .statusCode(204);
    }

    @Test
    public void patchOrderTest(){

        String orderId = placeOrderAndGetId();
        String updatedCustomerName = "Tom Peterson";

        given()
                .pathParam("orderId", orderId)
                .header("Authorization", "Bearer " + ACCESS_TOKEN)
                .contentType(ContentType.JSON)
                .body(new UpdateOrderRequest(updatedCustomerName))
                .patch("/orders/{orderId}")
                .then()
                .statusCode(204);

        OrderDetailsResponse orderDetailsResponse = given()
                .pathParam("orderId", orderId)
                .header("Authorization", "Bearer " + ACCESS_TOKEN)
                .get("/orders/{orderId}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .response()
                .as(OrderDetailsResponse.class);
    }


    private String placeOrderAndGetId(){
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + ACCESS_TOKEN)
                .body(new SubmitOrderRequest(1, "Kevin Lee"))
                .post("/orders")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .extract()
                .response()
                .jsonPath()
                .getString("orderId");
    }


    private static String generateToken(){
        ClientRequestBody requestBody = new ClientRequestBody("Kevin Lee", "kevin.lee17@gmail.com");
        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(BASE_URL + "/api-clients")
                .then()
                .statusCode(201)
                .extract()
                .response();

        String accessToken = response.as(ApiClientResponseBody.class).getAccessToken();

        return accessToken;
    }
}
