//package com.mgnt.ticketing.domain.integration.base;
//
//import io.restassured.RestAssured;
//import io.restassured.response.ExtractableResponse;
//import io.restassured.response.Response;
//import org.junit.jupiter.api.BeforeEach;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.server.LocalServerPort;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.util.Map;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("test")
//public class BaseIntegrationTest {
//
//    @LocalServerPort
//    protected int port;
//
//    protected static final String LOCAL_HOST = "http://localhost:";
//
//    @Autowired
//    private DatabaseCleanup databaseCleanup;
//    @Autowired
//    private TestDataHandler testDataHandler;
//
//    @BeforeEach
//    public void setUpByBeforeEach() {
//        RestAssured.port = port;
//        // 데이터 초기화
//        databaseCleanup.execute();
//        // 초기 콘서트 정보 세팅
//        testDataHandler.settingConcertInfo();
//        // 초기 예약 세팅
//        testDataHandler.reserveSeats();
//    }
//
//    public static ExtractableResponse<Response> get(String path) {
//        return RestAssured
//                .given().log().all()
//                .when().get(path)
//                .then().log().all().extract();
//    }
//
//    public static ExtractableResponse<Response> get(String path, Map<String, ?> parametersMap) {
//        return RestAssured
//                .given().log().all()
//                .queryParams(parametersMap)
//                .when().get(path)
//                .then().log().all().extract();
//    }
//
//    public static <T> ExtractableResponse<Response> post(String path) {
//        return RestAssured
//                .given().log().all()
//                .contentType(MediaType.APPLICATION_JSON_VALUE)
//                .when().post(path)
//                .then().log().all().extract();
//    }
//
//    public static <T> ExtractableResponse<Response> post(String path, T requestBody) {
//        return RestAssured
//                .given().log().all()
//                .body(requestBody)
//                .contentType(MediaType.APPLICATION_JSON_VALUE)
//                .when().post(path)
//                .then().log().all().extract();
//    }
//
//    public static <T> ExtractableResponse<Response> put(String path) {
//        return RestAssured
//                .given().log().all()
//                .contentType(MediaType.APPLICATION_JSON_VALUE)
//                .when().put(path)
//                .then().log().all().extract();
//    }
//
//    public static <T> ExtractableResponse<Response> put(String path, T requestBody) {
//        return RestAssured
//                .given().log().all()
//                .body(requestBody)
//                .contentType(MediaType.APPLICATION_JSON_VALUE)
//                .when().put(path)
//                .then().log().all().extract();
//    }
//
//    public static <T> ExtractableResponse<Response> patch(String path) {
//        return RestAssured
//                .given().log().all()
//                .contentType(MediaType.APPLICATION_JSON_VALUE)
//                .when().patch(path)
//                .then().log().all().extract();
//    }
//
//    public static <T> ExtractableResponse<Response> patch(String path, T requestBody) {
//        return RestAssured
//                .given().log().all()
//                .body(requestBody)
//                .contentType(MediaType.APPLICATION_JSON_VALUE)
//                .when().patch(path)
//                .then().log().all().extract();
//    }
//
//    public static ExtractableResponse<Response> delete(String path) {
//        return RestAssured
//                .given().log().all()
//                .when().delete(path)
//                .then().log().all().extract();
//    }
//
//    public static <T> ExtractableResponse<Response> delete(String path, T requestBody) {
//        return RestAssured
//                .given().log().all()
//                .body(requestBody)
//                .contentType(MediaType.APPLICATION_JSON_VALUE)
//                .when().delete(path)
//                .then().log().all().extract();
//    }
//}
