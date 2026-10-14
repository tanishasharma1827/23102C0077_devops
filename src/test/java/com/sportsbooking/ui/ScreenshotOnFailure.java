package com.sportsbooking.ui;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * JUnit 5 extension: when a UI test fails, saves a browser screenshot to
 * target/selenium-screenshots/&lt;TestClass&gt;_&lt;testMethod&gt;.png before the browser closes.
 */
public class ScreenshotOnFailure implements TestWatcher {

    static final Path DIR = Path.of("target", "selenium-screenshots");

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        Object test = context.getRequiredTestInstance();
        if (!(test instanceof BaseUiTest ui) || ui.driver == null) {
            return;
        }
        try {
            WebDriver driver = ui.driver;
            Path source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE).toPath();
            Files.createDirectories(DIR);
            Path target = DIR.resolve(context.getRequiredTestClass().getSimpleName() + "_"
                    + context.getRequiredTestMethod().getName() + ".png");
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[selenium] Failure screenshot saved: " + target.toAbsolutePath());
        } catch (Exception e) {
            System.out.println("[selenium] Could not save screenshot: " + e.getMessage());
        } finally {
            ui.quit();
        }
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        if (context.getRequiredTestInstance() instanceof BaseUiTest ui) {
            ui.quit();
        }
    }
}
