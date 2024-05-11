package api.books.simple.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static api.books.simple.api.TokenGenerator.getNewToken;
import static io.restassured.RestAssured.given;

public class ApiOperations {

    //make sure no one can instantiate this class
    private ApiOperations(){}

    private static final String AUTH_TOKEN;

    static{
        AUTH_TOKEN = "Bearer " + getNewToken();
    }

    public static Response performGetRequest(String endpoint, boolean requiresAuth){
        RequestSpecification requestSpecification = given();
        if (requiresAuth){
            requestSpecification = requestSpecification.header("Authorization", AUTH_TOKEN);
        }
        return requestSpecification
                .when()
                .get(endpoint);
    }

    public static Response performGetRequestQueryParam(String endpoint, String paramKey, String paramValue, boolean requiresAuth){
        RequestSpecification requestSpecification = given();
        if (requiresAuth){
            requestSpecification = requestSpecification.header("Authorization", AUTH_TOKEN);
        }
        return  requestSpecification
                .queryParam(paramKey, paramValue)
                .when()
                .get(endpoint);
    }

    public static Response performGetRequestPathParam(String endpoint, String paramKey, String paramValue, boolean requiresAuth){
        RequestSpecification requestSpecification = given();
        if (requiresAuth){
            requestSpecification = requestSpecification.header("Authorization", AUTH_TOKEN);
        }
        return   requestSpecification
                .pathParams(paramKey, paramValue)
                .when()
                .get(endpoint);
    }

    public static Response performPostRequest(String endpoint, Object payload, boolean requiresAuth){
        RequestSpecification requestSpecification = given();
        if (requiresAuth){
            requestSpecification = requestSpecification.header("Authorization", AUTH_TOKEN);
        }
        return   requestSpecification
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post(endpoint);
    }


    public static Response performPatchRequest(String endpoint, String paramKey, String paramValue, Object payload){
        return   given()
                .contentType(ContentType.JSON)
                .header("Authorization", AUTH_TOKEN)
                .body(payload)
                .pathParams(paramKey, paramValue)
                .when()
                .patch(endpoint);
    }

    public static Response performDeleteRequest(String endpoint, String paramKey, String paramValue) {
        return given()
                .header("Authorization", AUTH_TOKEN)
                .pathParams(paramKey, paramValue)
                .when()
                .delete(endpoint);
    }



}
