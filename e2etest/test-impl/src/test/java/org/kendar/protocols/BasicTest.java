package org.kendar.protocols;

import org.junit.jupiter.api.TestInfo;
import org.kendar.protocol.utils.NavigationUtils;
import org.kendar.protocol.utils.SeleniumIntegration;
import org.kendar.protocol.utils.Sleeper;
import org.kendar.protocol.utils.Utils;
import org.openqa.selenium.WebDriver;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.nio.file.Path;
import java.time.Duration;

public class BasicTest {
    private static Path root;
    private static Path projectRoot;
    private static ComposeContainer environment;
    private static String tpmHost;
    private Path storage;
    private SeleniumIntegration selenium;
    private NavigationUtils navigation;
    private WebDriver driver;

    public static void tearDownAfterClassBase(){
        environment.stop();
    }
    public void tearDownAfterEachBase(){
        Utils.setCache("driver", null);
        Utils.setCache("js", null);
        Sleeper.sleep(1000);
        selenium.takeMessageSnapshot("End of test");
        driver.quit();
        driver = null;
    }
    public static ComposeContainer getEnvironment() {
        return environment;
    }

    protected static Path getRoot() {
        return root;
    }

    protected static Path getProjectRoot() {
        return projectRoot;
    }

    protected static void setupDirectories(String project) throws Exception {
        root = Path.of(project).toAbsolutePath().
                getParent().getParent().getParent();
        projectRoot = Path.of(root.toString(), project);
        System.out.println(projectRoot);
    }

    protected static ComposeContainer setupContainer(String tpmHostExternal) throws Exception {
        tpmHost = tpmHostExternal;
        environment = new ComposeContainer(
                Path.of(getProjectRoot().toString(), "docker-compose.yml").toFile()
        );
        environment.withExposedService(tpmHost + "-debug", 5005)
                .withExposedService(tpmHost + "-admin", 8081)
                .withExposedService(tpmHost + "-proxy", 9000)
                .withExposedService(tpmHost + "-http", 80);
        environment.waitingFor(tpmHost + "-admin",
                Wait
                        .forHttp("/api/status")
                        .withStartupTimeout(Duration.ofSeconds(5)));

        return environment;
    }

    protected Path getStorage() {
        return storage;
    }

    protected SeleniumIntegration getSelenium() {
        return selenium;
    }

    protected NavigationUtils getNavigation() {
        return navigation;
    }

    protected WebDriver getDriver() {
        return driver;
    }

    protected void beforeEachBase(TestInfo testInfo) throws Exception {
        if (testInfo != null && testInfo.getTestClass().isPresent() &&
                testInfo.getTestMethod().isPresent()) {
            var className = testInfo.getTestClass().get().getSimpleName();
            var method = testInfo.getTestMethod().get().getName();

            if (testInfo.getDisplayName().startsWith("[")) {
                var dsp = testInfo.getDisplayName().replaceAll("[^a-zA-Z0-9_\\-,.]", "_");
                storage = Path.of(getRoot().toString(), "target", "tests", className, method, dsp);
            } else {
                storage = Path.of(getRoot().toString(), "target", "tests", className, method);
            }
        }
        Utils.getCache().clear();
        var proxyHost = getEnvironment().getServiceHost(tpmHost + "-proxy", 9000);
        var proxyPort = getEnvironment().getServicePort(tpmHost + "-proxy", 9000);

        selenium = new SeleniumIntegration(storage, proxyHost, proxyPort);
        navigation = new NavigationUtils(selenium);
        selenium.resettingDriver();
        driver = Utils.getCache("driver");

        Utils.setCache("selenium", selenium);
        Utils.setCache("storage", storage);
        Utils.setCache("navigationUtils", navigation);
        Utils.setCache("driver", driver);

        //Utils.killApacheLogger();
    }
}
