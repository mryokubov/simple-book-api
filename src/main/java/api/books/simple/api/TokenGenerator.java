package api.books.simple.api;

import com.github.javafaker.Faker;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static api.books.simple.api_constants.ApiEndPoints.*;
import static api.books.simple.api_constants.ApiStatus.CREATED;
import static api.books.simple.api_constants.ApiStatus.OK;
import static io.restassured.RestAssured.given;

public class TokenGenerator {

    private static final String CONFIG_FILE_PATH = "src/main/resources/config.properties";
    private static Faker faker = new Faker();

    private TokenGenerator(){}

    public static String getNewToken() {
        Properties properties = new Properties();
        try {
            FileInputStream inputStream = new FileInputStream(CONFIG_FILE_PATH);
            properties.load(inputStream);
            inputStream.close();

            String accessToken = properties.getProperty("accessToken");

            if (StringUtils.isBlank(accessToken) || !isValidToken(accessToken)) {
                accessToken = generateAndSaveToken(properties);
            }

            return accessToken;
        } catch (IOException e) {
            System.out.println("Error reading config.properties: " + ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    private static boolean isValidToken(String accessToken) {
        Response response = given()
                .header("Authorization", "Bearer " + accessToken)
                .get(BASE_URI + GET_All_ORDERS_ENDPOINT)
                .andReturn();

        return response.getStatusCode() == OK;
    }

    private static String generateAndSaveToken(Properties properties) {
        String clientName = generateRandomClientName();

        String[] emailDomains = {
                "gmail.com",
                "yahoo.com",
                "mail.ru",
                "outlook.com",
                "hotmail.com",
                "icloud.com",
                "aol.com",
                "protonmail.com",
                "yandex.com",
                "gmx.com"
        };

        String name = Arrays.stream(clientName.split(" "))
                .map(x -> x.toLowerCase().replace(".", ""))
                .collect(Collectors.joining("."));


        String clientEmail = name + "@" + emailDomains[(int)(Math.random() * emailDomains.length)];

        String payload =
                "{\n" +
                        "  \"clientName\": \"" + clientName + "\",\n" +
                        "  \"clientEmail\": \"" + clientEmail + "\"\n" +
                        "}";

        Response response = given()
                .contentType(ContentType.JSON)
                .body(payload)
                .post(POST_REGISTER_CLIENT_ENDPOINT)
                .andReturn();

        if (response.getStatusCode() == CREATED) {
            String accessToken = response.jsonPath().getString("accessToken");
            properties.setProperty("accessToken", accessToken);

            try {
                FileOutputStream outputStream = new FileOutputStream(CONFIG_FILE_PATH);
                properties.store(outputStream, "API Configuration");
                outputStream.close();
            } catch (IOException e) {
                System.out.println("Error writing to config.properties: " + ExceptionUtils.getStackTrace(e));
            }

            return accessToken;
        } else {
            System.out.println("Failed to register client: " + response.getBody().asString());
            return null;
        }
    }

    private static String generateRandomClientName(){
        return faker.name().fullName();
    }

    public static void main(String[] args) {
        System.out.println(getNewToken());
    }
}

