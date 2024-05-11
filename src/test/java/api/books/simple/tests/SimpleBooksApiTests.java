package api.books.simple.tests;

import static api.books.simple.api.ApiOperations.*;
import static api.books.simple.api_constants.ApiStatus.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.CoreMatchers.*;
import static api.books.simple.api_constants.ApiEndPoints.*;
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

    @BeforeClass
    public static void setUp(){
        RestAssured.baseURI = BASE_URI;
    }


    @Test
    public void getApiStatusTest(){
        performGetRequest(GET_STATUS_ENDPOINT, false)
                .then()
                .statusCode(OK)
                .contentType(ContentType.JSON)
                .body("status", equalTo("OK"));
    }

    @Test
    public void getListOfBooksTest(){
        performGetRequest(GET_ALL_BOOKS_ENDPOINT, false)
                .then()
                .statusCode(OK)
                .contentType(ContentType.JSON)
                .body("", Matchers.instanceOf(List.class))
                .body("size()", equalTo(6));
    }

    @Test
    public void getListOfBooksVerifyEachTest(){
        Response response =  performGetRequest(GET_ALL_BOOKS_ENDPOINT, false)
                .then()
                .statusCode(OK)
                .contentType(ContentType.JSON)
                .extract()
                .response();

        BookResponse[] booksResponse = response.as(BookResponse[].class);
        for (BookResponse bookLimitedDetailsResponse : booksResponse) {
            Assert.assertTrue(bookLimitedDetailsResponse.getId() != null);
        }
    }

    @Test
    public void getListOfBooksWithTypeQueryParamTest(){
        performGetRequestQueryParam(GET_ALL_BOOKS_ENDPOINT, "type", "fiction", false)
                .then()
                .statusCode(OK)
                .contentType(ContentType.JSON)
                .body("", Matchers.instanceOf(List.class))
                .body("size()", equalTo(4));
    }

    @Test
    public void getListOfBooksWithLimitQueryParamTest(){
        performGetRequestQueryParam(GET_ALL_BOOKS_ENDPOINT, "limit", String.valueOf(2), false)
                .then()
                .statusCode(OK)
                .contentType(ContentType.JSON)
                .body("", Matchers.instanceOf(List.class))
                .body("size()", equalTo(2));
    }

    @Test
    public void getSingleBookByIdTest(){
        performGetRequestPathParam(GET_ONE_BOOK_ENDPOINT, "bookId", String.valueOf(2), false)
                .then()
                .statusCode(OK)
                .contentType(ContentType.JSON)
                .body("id", equalTo(2));
    }

    @Test
    public void getSingleBookByIdValidateAllFieldsJSONPathTest(){

        Response response = performGetRequestPathParam(GET_ONE_BOOK_ENDPOINT, "bookId", String.valueOf(1), false)
                .then()
                .statusCode(OK)
                .contentType(ContentType.JSON)
                .body("id", equalTo(1))
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

        Response response = performGetRequestPathParam(GET_ONE_BOOK_ENDPOINT, "bookId", String.valueOf(1), false)
                .then()
                .statusCode(OK)
                .contentType(ContentType.JSON)
                .body("id", equalTo(1))
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
        performGetRequestPathParam(GET_ONE_BOOK_ENDPOINT, "bookId", String.valueOf(20), false)
                .then()
                .statusCode(NOT_FOUND)
                .contentType(ContentType.JSON)
                .body("error", equalTo("No book with id 20"));
    }

    @Test
    public void postSubmitBookOrderTest(){
        SubmitOrderRequest requestPayload = new SubmitOrderRequest(1, "Kevin Lee");

        performPostRequest(POST_ORDERS_ENDPOINT, requestPayload, true)
                .then()
                .statusCode(CREATED)
                .contentType(ContentType.JSON)
                .body("created", equalTo(true))
                .body("orderId", notNullValue());
    }

    @Test
    public void postSubmitBookOrderBadTest(){

        String payload = "{\n" +
                "    \"bookId\": 1,\n" +
                "    \"customerName\": \"Mike Lee\"\n" +
                "}";

        performPostRequest(POST_ORDERS_ENDPOINT,
                payload, true)
                .then()
                .statusCode(CREATED)
                .contentType(ContentType.JSON)
                .body("created", equalTo(true))
                .body("orderId", notNullValue());
    }

    @Test
    public void postSubmitBookOrderWithNoAccessTokenTest(){
        String payload = "{\n" +
                "    \"bookId\": 1,\n" +
                "    \"customerName\": \"Mike Lee\"\n" +
                "}";

        performPostRequest(POST_ORDERS_ENDPOINT,
                payload, false)
                .then()
                .statusCode(UNAUTHORIZED)
                .contentType(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body("error", equalTo("Missing Authorization header."));
    }


    @Test
    public void getAllOrdersTest(){
        performGetRequest(GET_All_ORDERS_ENDPOINT, true)
                .then()
                .statusCode(OK)
                .contentType(ContentType.JSON)
                .body("", Matchers.instanceOf(List.class));
    }

    @Test
    public void getSingleOrderTest(){
        String orderId = placeOrderAndGetId();
        performGetRequestPathParam(GET_ONE_ORDER_ENDPOINT, "orderId", orderId, true)
                .then()
                .statusCode(OK)
                .contentType(ContentType.JSON)
                .body("id", equalTo(orderId));
    }


    @Test
    public void deleteOrderTest(){
        String orderId = placeOrderAndGetId();
        performDeleteRequest(DELETE_ONE_ORDER_ENDPOINT, "orderId", orderId)
                .then()
                .statusCode(NO_CONTENT);
    }

    @Test
    public void patchOrderTest(){

        String orderId = placeOrderAndGetId();
        String updatedCustomerName = "Tom Peterson";

        performPatchRequest(PATCH_ONE_ORDER_ENDPOINT, "orderId", orderId, new UpdateOrderRequest(updatedCustomerName))
                .then()
                .statusCode(NO_CONTENT);


        performGetRequestPathParam(GET_ONE_ORDER_ENDPOINT, "orderId", orderId, true)
                .then()
                .statusCode(OK)
                .contentType(ContentType.JSON)
                .body("customerName", equalTo(updatedCustomerName));
    }


    private String placeOrderAndGetId(){
        return performPostRequest(POST_ORDERS_ENDPOINT, new SubmitOrderRequest(1, "Kevin Lee"), true)
                .then()
                .statusCode(CREATED)
                .contentType(ContentType.JSON)
                .extract()
                .response()
                .jsonPath()
                .getString("orderId");
    }


    private static String generateToken(){
        ClientRequest requestBody = new ClientRequest("Kevin Lee", "kevin.lee17@gmail.com");
        Response response = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(BASE_URI+POST_REGISTER_CLIENT_ENDPOINT)
                .then()
                .statusCode(201)
                .extract()
                .response();

        String accessToken = response.as(ApiClientResponseBody.class).getAccessToken();
        return accessToken;
    }
}
