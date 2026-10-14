package com.sportsbooking.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;
import java.util.UUID;

/**
 * Common setup for Selenium tests.
 * By default the app is started on a random port inside the test.
 * Pass -Dapp.baseUrl=http://host:port/path to test an already deployed server instead (Task 10).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(ScreenshotOnFailure.class)
public abstract class BaseUiTest {

    protected static final String MANAGER_EMAIL = "manager@sports.local";

    @LocalServerPort
    private int port;

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected String baseUrl;

    @BeforeEach
    void openBrowser() {
        String configured = System.getProperty("app.baseUrl", "");
        baseUrl = configured.isBlank() ? "http://localhost:" + port : configured.replaceAll("/$", "");

        ChromeOptions options = new ChromeOptions();
        if (!"false".equals(System.getProperty("selenium.headless", "true"))) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1280,900", "--no-sandbox", "--disable-dev-shm-usage");
        driver = new ChromeDriver(options);   // Selenium Manager downloads a matching chromedriver
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    void quit() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    // ---------- small page helpers used by the tests ----------

    protected void open(String path) {
        driver.get(baseUrl + path);
    }

    protected WebElement find(String cssSelector) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(cssSelector)));
    }

    protected void type(String cssSelector, String text) {
        WebElement field = find(cssSelector);
        field.clear();
        field.sendKeys(text);
    }

    protected String uniqueEmail(String name) {
        return name + "-" + UUID.randomUUID().toString().substring(0, 8) + "@test.local";
    }

    protected void register(String name, String email, String phone) {
        open("/register");
        type("#name", name);
        type("#email", email);
        type("#phone", phone);
        find("#register-submit").click();
    }

    protected void signIn(String email) {
        open("/signin");
        type("#signin-email", email);
        find("#signin-submit").click();
    }

    protected void signOut() {
        find("#nav-signout").click();
        find("#nav-register");   // wait until the signed-out home page has loaded
    }

    /** Opens the slots page of a facility and clicks the first Book button; returns the booked time label. */
    protected String bookFirstFreeSlot(long facilityId, String date) {
        open("/slots?facilityId=" + facilityId + "&date=" + date);
        WebElement button = find("button[id^='book-']");
        String time = button.findElement(By.xpath("./ancestor::tr/td[1]")).getText();
        button.click();
        return time;
    }

    protected String flashMessage() {
        return find("#flash-message").getText();
    }

    protected String flashError() {
        return find("#flash-error").getText();
    }
}
