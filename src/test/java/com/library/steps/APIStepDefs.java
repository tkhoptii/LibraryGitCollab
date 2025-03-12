package com.library.steps;

import com.library.pages.BookPage;
import com.library.pages.LoginPage;
import com.library.utility.DB_Util;
import com.library.utility.DatabaseHelper;
import com.library.utility.LibraryAPI_Util;
import io.cucumber.java.en.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;

import java.util.AbstractSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.*;

public class APIStepDefs {


    RequestSpecification givenPart = given().log().all();
    Response response;
    JsonPath jp;
    ValidatableResponse thenPart;

    Map<String, Object> randomData = new HashMap<>();
    Map<String, String> expectedIdValue = new HashMap<>();

    @Given("I logged Library api as a {string}")
    public void i_logged_library_api_as_a(String role) {
        givenPart.header("x-library-token", LibraryAPI_Util.getToken(role));

    }

    @Given("Accept header is {string}")
    public void accept_header_is(String acceptHeader) {
        givenPart.accept(acceptHeader);
    }

    @When("I send GET request to {string} endpoint")
    public void i_send_get_request_to_endpoint(String endpoint) {
        response = givenPart.get(endpoint);
        response.prettyPeek();
        jp = response.jsonPath();
        thenPart = response.then();
    }

    @Then("status code should be {int}")
    public void status_code_should_be(Integer expectedStatusCode) {
        thenPart.statusCode(expectedStatusCode);
    }

    @Then("Response Content type is {string}")
    public void response_content_type_is(String expectedContentType) {
        thenPart.contentType(expectedContentType);
    }

    @Then("Each {string} field should not be null")
    public void each_field_should_not_be_null(String eachField) {
        Assert.assertNotNull(eachField);
    }

    //us2

    @Given("Path param {string} is {string}")
    public void path_param_is(String pathParamKey, String pathParamValue) {
        givenPart.pathParam(pathParamKey, pathParamValue);
        expectedIdValue.put(pathParamKey, pathParamValue);
    }

    @Then("{string} field should be same with path param")
    public void field_should_be_same_with_path_param(String pathParamKey) {
        String actualPathParValue = jp.getString(pathParamKey);
        System.out.println("pathParValue = " + actualPathParValue);

        // Map <String, String > expectedIdValue = new HashMap<>();
        //expectedIdValue.put("id", "1");

        String expectedValue = expectedIdValue.get("id");
        System.out.println("expectedIdValue = " + expectedIdValue);
        Assert.assertEquals(expectedValue, actualPathParValue);

    }


    @Then("following fields should not be null")
    public void following_fields_should_not_be_null(List<Object> fields) {
        for (Object each : fields) {
            Assert.assertNotNull(each);
        }
    }

    //us3

    @Given("Request Content Type header is {string}")
    public void request_content_type_header_is(String requestContentType) {
        givenPart.contentType(requestContentType); // correct?

    }

    @Given("I create a random {string} as request body")
    public void i_create_a_random_as_request_body(String dataType) {

        switch (dataType) {
            case "book":
                randomData = LibraryAPI_Util.getRandomBookMap();
                break;
            case "user":
                randomData = LibraryAPI_Util.getRandomUserMap();
                break;
            default:
                throw  new RuntimeException("Invalid data type" + dataType);
        }
        givenPart.formParams(randomData);

    }

    @When("I send POST request to {string} endpoint")
    public void i_send_post_request_to_endpoint(String dataTypeEndpoint) {
        response = givenPart.post(dataTypeEndpoint);
        response.prettyPeek();
        jp = response.jsonPath();
        thenPart = response.then();
    }

    @Then("the field value for {string} path should be equal to {string}")
    public void the_field_value_for_path_should_be_equal_to(String messagePath, String expectedMessage) {
        Assert.assertEquals(expectedMessage, jp.getString(messagePath));
    }

    @Then("{string} field should not be null")
    public void field_should_not_be_null(String field) {
        Assert.assertNotNull(field);
    }

    //us 3 scenario 2

    @Then("UI, Database and API created book information must match")
    public void ui_database_and_api_created_book_information_must_match() {

        int id = jp.getInt("book_id");
        String query = "select name, isbn, year, author, book_category_id, description from books where id = " + id;
        //String query = DatabaseHelper.getBookByIdQuery(String.valueOf(id));
        DB_Util.runQuery(query);
        Map<String, Object> actualData = DB_Util.getRowMap(1);

        Assert.assertEquals(randomData, actualData);
    }

    //us4 sc 2

    @Then("created user information should match with Database")
    public void created_user_information_should_match_with_database() {

        int id = jp.getInt("user_id");
        String query = DatabaseHelper.getUserByIdQuery(id);
        DB_Util.runQuery(query);
        Map<String, Object> actualUserDataDB = DB_Util.getRowMap(1);
        String password = (String) randomData.remove("password");
        Assert.assertEquals(randomData, actualUserDataDB);
        randomData.put("password", password);
    }

    @Then("created user should be able to login Library UI")
    public void created_user_should_be_able_to_login_library_ui() {

        LoginPage loginPage = new LoginPage();
        String email = (String) randomData.get("email");
        String password = (String) randomData.get("password");
        loginPage.login(email, password);

    }

    @Then("created user name should appear in Dashboard Page")
    public void created_user_name_should_appear_in_dashboard_page() {

        BookPage bookPage = new BookPage();
        String fullNameFromUI = bookPage.accountHolderName.getText();
        String fullNameAPI = (String) randomData.get("full_name");

        Assert.assertEquals(fullNameAPI, fullNameFromUI);
    }

    //us5

    @Given("I logged Library api with credentials {string} and {string}")
    public void i_logged_library_api_with_credentials_and(String email, String password) {
        LibraryAPI_Util.getToken(email, password);

    }

    @Given("I send {string} information as request body")
    public void i_send_information_as_request_body(String token) {
        givenPart.body(token);
    }

}
