package de.unimuenster.imi.randimi.selenium.supportFunctions;

import java.net.InetAddress;
import java.net.UnknownHostException;

import de.unimuenster.imi.randimi.RandimiTest;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.testcontainers.containers.*;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import lombok.Setter;

import java.time.Duration;

import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Value;
import org.testcontainers.utility.DockerImageName;

/**
 * A class with the main functions needed for testing.
 * @author Anika Herbermann
 */
@Testcontainers
@ExtendWith({SeleniumScreenshotOnFailureExtension.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class SeleniumBaseTest extends RandimiTest {

    @Setter
    private boolean logoutWanted = true;

    private boolean beforeAllWanted = true;

    private RemoteWebDriver driver;
    protected SeleniumBaseFunctions basics;
    protected String host;

    @Value("${app.version}")
    protected String randimiVersion;

    @Value("${spring.datasource.url}")
    protected String dbUrl;
    @Value("${spring.datasource.username}")
    protected String dbUser;
    @Value("${spring.datasource.password}")
    protected String dbPassword;

    @Value("${spring.flyway.locations}")
    private String[] flywayLocations;

    /**
     * Port of the instance started by the unit test.
     */
    @LocalServerPort
    private int localServerPort;

    /**
     * Context path of the instance started by the unit test.
     */
    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * Context path of the instance used by selenium.
     */
    private String randimiContextPath;

    /**
     * Port of the instance used by selenium.
     */
    private int randimiSeleniumPort;

    /**
     * If the selenium tests should run randimi in Docker instead of using the instance started by JUnit.
     */
    @Value("${randimi.selenium.run-randimi-in-docker}")
    private boolean runRandimiInDocker;

    /**
     * Docker image of randimi.
     */
    @Value("${randimi.selenium.image}")
    private String randimiImage;

    private static final Network network = Network.newNetwork();

    private PostgreSQLContainer<?> postgreSQLContainer = null;
    private GenericContainer<?> randimiContainer = null;

    @Container
    private final BrowserWebDriverContainer<?> container = new BrowserWebDriverContainer<>()
            .withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.SKIP, null)
            .withCapabilities(new ChromeOptions().addArguments("--disable-dev-shm-usage", "--headless", "--window-size=1920,1080"));

    public SeleniumBaseTest() {
        try {
            // Old version of docker (tool)
            host = container.getTestHostIpAddress();
        } catch (UnsupportedOperationException e) {
            // Windows
            if (System.getProperty("os.name").startsWith("Windows"))
                try {
                    host = InetAddress.getLocalHost().getHostAddress();
                } catch (UnknownHostException ex) {
                    host = container.getHost(); //fallback: returns "localhost" and is not working :-/
                }
            else // Linux
                host = "172.17.0.1";
        }
    }

    /**
     * Fills the database with test data.
     */
    @BeforeEach
    public void setupSelenium() {
        if (runRandimiInDocker) {
            if (postgreSQLContainer == null) {
                postgreSQLContainer = new PostgreSQLContainer<>("postgres:16")
                        .withNetwork(network)
                        .withNetworkAliases("randimi-db")
                        .withEnv("POSTGRES_PASSWORD", "password")
                        .withDatabaseName("randimi_db")
                        .withUsername("randomuser")
                        .withPassword("password");
            }

            if (randimiContainer == null ) {
                randimiContainer = new GenericContainer<>(DockerImageName.parse(randimiImage))
                        .withExposedPorts(8080)
                        .withNetwork(network)
                        .withNetworkAliases("randimi")
                        .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://randimi-db:5432/randimi_db")
                        .withEnv("SPRING_DATASOURCE_USERNAME", "randomuser")
                        .withEnv("SPRING_DATASOURCE_PASSWORD", "password")
                        .withEnv("SPRING_PROFILES_ACTIVE", "dev");
            }
            postgreSQLContainer.start();
            randimiContainer.start();
            randimiSeleniumPort = randimiContainer.getMappedPort(8080);
            randimiContextPath = "";
        } else {
            randimiSeleniumPort = localServerPort;
            randimiContextPath = contextPath;
        }

        if (beforeAllWanted && !runRandimiInDocker) {
            createData();
            beforeAllWanted = false;
        }
        prepare();
    }

    @AfterEach
    public void afterEach() {
        if (runRandimiInDocker) {
            randimiContainer.stop();
            postgreSQLContainer.stop();
        }
    }

    /**
     * Refreshes the driver and initialises basic functions.
     * Required in beforeEach of test classes.
     */
    public void prepare() {
        // Refreshing driver
        driver = container.getWebDriver();
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(20));

        // Giving access to basic test functions.
        final String baseUrl = "http://" + host + ":" + randimiSeleniumPort + randimiContextPath;
        basics = new SeleniumBaseFunctions(driver, baseUrl);

        basics.loadMainPage();
    }

    /**
     * Quits the current browser session and sets back all database changes.
     */
    @AfterEach
    public void tearDown() {
        if(logoutWanted) {
            basics.logout();
        }
        else {
            logoutWanted = true;
        }

        if (driver == null) throw new Error("Driver has been closed prematurely.");
        driver.quit();

        if (!runRandimiInDocker) {
            recoverData();
        }
    }

    /**
     * Tests, whether all labels, headlines or example texts possess a translation.
     * All HTML elements with translated texts except for errors need 'selenium-language-test' as class annotation.
     */
    @Test
    public void languageTest() {
        SeleniumLanguageAutomation autoLanguageTest = new SeleniumLanguageAutomation(basics);
        autoLanguageTest.languageCheckLabel();
    }

    /**
     * A test that checks, if all main elements of a page are indeed visible.
     */
    @Test
    public abstract void visibilityTest();

    /**
     * Inserts test data into the database.
     */
    public void createData() {
        final Flyway flyway = configureFlyway();
        flyway.clean();
        flyway.migrate();
    }

    /**
     * Sets up all test data to ist original values.
     */
    public void recoverData() {
        final Flyway flyway = configureFlyway();
        flyway.clean();
        flyway.migrate();
    }

    private Flyway configureFlyway() {
        return Flyway.configure()
                     .cleanDisabled(false)
                     .locations(flywayLocations)
                     .dataSource(dbUrl, dbUser, dbPassword)
                     .load();
    }
}
