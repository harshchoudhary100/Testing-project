package royalbrothers.utils;

import com.microsoft.playwright.*;
import java.util.Arrays;
import java.nio.file.Paths; // Import for Video paths if needed

public class DriverFactory {
    // ThreadLocal ensures parallel execution safety
    private static ThreadLocal<Playwright> playwright = new ThreadLocal<>();
    private static ThreadLocal<Browser> browser = new ThreadLocal<>();
    private static ThreadLocal<BrowserContext> context = new ThreadLocal<>();
    private static ThreadLocal<Page> page = new ThreadLocal<>();

    public static Page getPage() {
        // Init driver if page or playwright is null
        if (playwright.get() == null || page.get() == null) {
            initDriver();
        }
        return page.get();
    }

    public static void initDriver() {
        System.out.println("Initializing Playwright Driver...");

        playwright.set(Playwright.create());

        // 1. Launch Browser (Visible mode)
        browser.set(playwright.get().chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false) // Show browser
                .setSlowMo(100)     // Slow down slightly for stability
                .setArgs(Arrays.asList("--start-maximized")))); // Attempt to maximize window

        // 2. Create Context with FIXED Desktop Viewport
        // Royal Brothers switches to Mobile view on small screens, so 1920x1080 is critical.
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setViewportSize(1920, 1080)
                .setIgnoreHTTPSErrors(true); // Handle SSL issues if any

        // OPTIONAL: Record Video for debugging failures (Uncomment below line to enable)
        // contextOptions.setRecordVideoDir(Paths.get("target/videos/"));

        context.set(browser.get().newContext(contextOptions));

        // 3. Create Page
        page.set(context.get().newPage());
    }

    public static void closeDriver() {
        try {
            if (page.get() != null) {
                page.get().close();
                page.remove();
            }
            if (context.get() != null) {
                // Save video if recording is enabled
                context.get().close();
                context.remove();
            }
            if (browser.get() != null) {
                browser.get().close();
                browser.remove();
            }
            if (playwright.get() != null) {
                playwright.get().close();
                playwright.remove();
            }
            System.out.println("Driver closed successfully.");
        } catch (Exception e) {
            System.out.println("Error closing driver: " + e.getMessage());
        }
    }
}