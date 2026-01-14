package royalbrothers.stepdefinitions;

import royalbrothers.pages.HomePage;
import royalbrothers.pages.ListingPage;
import royalbrothers.utils.DriverFactory;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;

import java.util.Map;

public class BookingSteps {

    HomePage homePage = new HomePage(DriverFactory.getPage());
    ListingPage listingPage = new ListingPage(DriverFactory.getPage());

    @Given("I navigate to the Royal Brothers homepage")
    public void navigateToHome() {
        homePage.navigateToUrl("https://www.royalbrothers.com/");
    }

    @When("I select the city {string}")
    public void selectCity(String city) {
        homePage.selectCity(city);
    }

    @And("I enter the booking details")
    public void enterBookingDetails(DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);
        homePage.enterBookingDetails(data);
    }

    @And("I click on Search")
    public void clickSearch() {
        homePage.clickSearch();
    }

    @And("I click on Search without entering dates")
    public void clickSearchNoDates() {
        homePage.clickSearch();
    }

    @Then("I verify the date and time filter is visible on results page")
    public void verifyFilterVisibility() {
        listingPage.validateFilterVisibility();
    }

    @When("I apply the location filter {string}")
    public void applyLocationFilter(String location) {
        listingPage.applyLocationFilter(location);
    }

    @Then("I collect all bike models and their availability")
    public void collectData() {
        // Data already collected in next step
    }

    @And("I validate that all displayed bikes are available at {string}")
    public void validateBikeLocations(String location) {
        listingPage.collectAndValidateBikeData(location);
    }

    @Then("I should see a validation message requesting date selection")
    public void verifyValidationMessage() {
        Assertions.assertTrue(homePage.isValidationErrorVisible(), "Error message was not displayed!");
    }
}