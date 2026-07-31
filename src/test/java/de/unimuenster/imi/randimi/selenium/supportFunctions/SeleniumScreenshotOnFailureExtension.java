package de.unimuenster.imi.randimi.selenium.supportFunctions;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SeleniumScreenshotOnFailureExtension implements AfterTestExecutionCallback {

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
        if (extensionContext.getExecutionException().isPresent()) {
            Object testInstance = extensionContext.getRequiredTestInstance();
            Field driverField = testInstance.getClass().getSuperclass().getDeclaredField("driver");
            driverField.setAccessible(true);
            RemoteWebDriver driver = (RemoteWebDriver) driverField.get(testInstance);
            byte[] screenshot = driver.getScreenshotAs(OutputType.BYTES);

            try {
                Path path = Paths
                        .get("target/selenium-screenshots")
                        .resolve(String.format("%s-%s-%s.png",
                                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss").format(LocalDateTime.now()),
                                extensionContext.getRequiredTestClass().getName(),
                                extensionContext.getRequiredTestMethod().getName()));

                Files.createDirectories(path.getParent());
                Files.write(path, screenshot);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}