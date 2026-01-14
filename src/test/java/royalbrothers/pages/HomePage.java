package royalbrothers.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.time.LocalDate;
import java.util.Map;

public class HomePage {
    private final Page page;
    private boolean dialogAppeared = false;

    // Dynamic Locators
    private String activeContainer = "";
    private String pickupDateId = "";
    private String pickupTimeId = "";
    private String dropoffDateId = "";
    private String dropoffTimeId = "";
    private String searchBtnSelector = "";

    public HomePage(Page page) {
        this.page = page;
        page.onDialog(dialog -> {
            System.out.println("Alert detected: " + dialog.message());
            dialogAppeared = true;
            try { dialog.accept(); } catch (Exception e) { }
        });
    }

    public void navigateToUrl(String url) {
        page.navigate(url);
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        System.out.println("Navigated to: " + url);
    }

    public void selectCity(String city) {
        page.waitForTimeout(2000);

        if (page.isVisible("#modal-city")) {
            System.out.println("City Modal visible. Selecting: " + city);
            Locator cityBox = page.locator(".city-box-true").filter(new Locator.FilterOptions().setHasText(city)).first();
            if (cityBox.isVisible()) {
                cityBox.click();
            } else {
                Locator modalInput = page.locator("#autocomplete-input");
                modalInput.fill(city);
                page.waitForTimeout(1000);
                page.keyboard().press("ArrowDown");
                page.keyboard().press("Enter");
            }
        } else {
            System.out.println("Using Global Search.");
            Locator cityInput = page.locator("#autocomplete-input").first();
            cityInput.evaluate("el => el.click()");
            cityInput.fill("");
            cityInput.pressSequentially(city, new Locator.PressSequentiallyOptions().setDelay(100));
            page.waitForTimeout(1500);
            page.keyboard().press("ArrowDown");
            page.keyboard().press("Enter");
        }

        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        determineActiveForm();
    }

    private void determineActiveForm() {
        if (page.locator("#booking-pc").isVisible()) {
            System.out.println("Desktop View Detected.");
            activeContainer = "#booking-pc";
            pickupDateId = "#pickup-date-other";
            pickupTimeId = "#pickup-time-other";
            dropoffDateId = "#dropoff-date-other";
            dropoffTimeId = "#dropoff-time-other";
            searchBtnSelector = "#booking-pc button[type='submit']";
        } else {
            System.out.println("Mobile/Tablet View Detected.");
            activeContainer = "#booking-mobile";
            pickupDateId = "#pickup-date-desk";
            pickupTimeId = "#pickup-time-desk";
            dropoffDateId = "#dropoff-date-desk";
            dropoffTimeId = "#dropoff-time-desk";
            searchBtnSelector = "#booking-mobile button[type='submit']";
        }
    }

    public void enterBookingDetails(Map<String, String> details) {
        System.out.println("Entering details...");
        String pickupDate = calculateDate(details.get("Pickup Date"));
        String dropoffDate = calculateDate(details.get("Dropoff Date"));
        String pickupTime = formatTime(details.get("Pickup Time"));
        String dropoffTime = formatTime(details.get("Dropoff Time"));

        openCalendarAndSelectDate(pickupDateId, pickupDate);
        selectTimeFromPicker(pickupTimeId, pickupTime);
        openCalendarAndSelectDate(dropoffDateId, dropoffDate);
        selectTimeFromPicker(dropoffTimeId, dropoffTime);

        System.out.println("Details entered successfully.");
    }

    private void openCalendarAndSelectDate(String inputId, String dateToSelect) {
        Locator input = page.locator(inputId);
        input.evaluate("el => el.click()");

        try {
            String rootId = inputId + "_root";
            page.locator(rootId + " .picker__holder").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(2000));
        } catch (Exception e) {
            input.click(new Locator.ClickOptions().setForce(true));
        }

        String rootId = inputId + "_root";
        Locator day = page.locator(rootId + " .picker__day--infocus").filter(new Locator.FilterOptions().setHasText(getDayFromDate(dateToSelect))).first();

        if(!day.isVisible()) {
            day = page.locator(rootId + " .picker__day--infocus:not(.picker__day--disabled)").first();
        }

        day.evaluate("el => el.click()");
        page.waitForTimeout(500);
    }

    private void selectTimeFromPicker(String inputId, String timeText) {
        System.out.println("Selecting Time: " + timeText);
        Locator input = page.locator(inputId);
        input.scrollIntoViewIfNeeded();
        input.click(new Locator.ClickOptions().setForce(true));
        page.waitForTimeout(1000);

        String rootId = inputId + "_root";
        Locator timeOption = page.locator(rootId + " .picker__list-item").filter(new Locator.FilterOptions().setHasText(timeText)).first();

        try {
            if (timeOption.count() > 0) {
                timeOption.evaluate("el => el.scrollIntoView()");
                timeOption.evaluate("el => el.click()");
            } else {
                page.locator(rootId + " .picker__list-item").first().evaluate("el => el.click()");
            }
        } catch (Exception e) { }
        page.waitForTimeout(500);
    }

    public void clickSearch() {
        dialogAppeared = false;
        System.out.println("Clicking Search...");
        Locator btn = page.locator(searchBtnSelector);

        try {
            page.waitForNavigation(new Page.WaitForNavigationOptions().setTimeout(10000), () -> {
                btn.evaluate("el => el.click()");
            });
            System.out.println("Search Submitted.");
        } catch (Exception e) {
            System.out.println("URL did not change. Checking errors...");

            // FIX: Added .first() to prevent Strict Mode Error here
            if (page.locator(".pickup-error").first().isVisible()) {
                System.out.println("Form Error: " + page.locator(".pickup-error").first().textContent());
            }

            try {
                page.locator(activeContainer + " form").evaluate("form => form.submit()");
            } catch (Exception ex) {}
        }
    }

    public boolean isValidationErrorVisible() {
        if (dialogAppeared) return true;

        // FIX: Added .first() everywhere to prevent Strict Mode Error
        boolean elementError = page.locator(".pickup-error, .dropoff-error, .toast-message").first().isVisible();
        boolean textError = page.getByText("Please select Date!").first().isVisible() ||
                page.getByText("Please select Time!").first().isVisible();

        System.out.println("Validation visible: " + (elementError || textError));
        return elementError || textError;
    }

    private String calculateDate(String input) {
        if (input != null && input.contains("+")) {
            long days = Long.parseLong(input.replace("+", "").replace(" days", "").trim());
            return LocalDate.now().plusDays(days).toString();
        }
        return LocalDate.now().toString();
    }

    private String getDayFromDate(String yyyyMmDd) {
        return String.valueOf(LocalDate.parse(yyyyMmDd).getDayOfMonth());
    }

    private String formatTime(String time) {
        if (time.startsWith("0") && time.contains(":")) {
            return time.substring(1);
        }
        return time;
    }
}