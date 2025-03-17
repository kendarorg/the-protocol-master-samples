package org.kendar.protocols;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.core5.http.HttpHost;
import org.junit.jupiter.api.TestInfo;
import org.kendar.protocol.utils.*;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.FakeStrategy;
import org.testcontainers.containers.wait.strategy.PortWaitStrategy;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

@SuppressWarnings("resource")
public class BasicTest {
    private static Path root;
    private static Path projectRoot;
    private static ComposeContainer environment;
    private static String tpmHost;
    private static HashMap<String, Integer> toWaitFor;
    private Path storage;
    private SeleniumIntegration selenium;
    private NavigationUtils navigation;
    private WebDriver driver;
    private String proxyHost;
    private Integer proxyPort;

    public static void tearDownAfterClassBase() {
        environment.stop();
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
        toWaitFor = new HashMap<>();
        environment = new ComposeContainer(
                Path.of(getProjectRoot().toString(), "docker-compose-testcontainers.yml").toFile()
        );
        toWaitFor.put(tpmHost, 8081);
        withExposedServiceHidden(tpmHost, 5005);
        withExposedService(tpmHost, 8081);
        withExposedService(tpmHost, 9000);
        withExposedService(tpmHost, 80);
        return environment;
    }

    protected static void startContainers() {
        environment.start();
        for (var item : toWaitFor.entrySet()) {
            waitPortAvailable(item.getKey(), item.getValue());
        }
        System.out.println("Containers started");
    }

    public static ComposeContainer withExposedService(String host, int ports) throws Exception {
        //environment.withExposedService(host, mainPort);
        environment.withExposedService(host, ports,
                new PortWaitStrategy().
                        forPorts(ports).
                        withStartupTimeout(Duration.ofSeconds(5)));
        toWaitFor.put(host, ports);
        return environment;
    }

    public static ComposeContainer withExposedServiceHidden(String host, int ports) throws Exception {
        //environment.withExposedService(host, mainPort);
        environment.withExposedService(host, ports,
                new FakeStrategy());
        toWaitFor.put(host, ports);
        return environment;
    }

    public static void waitPortAvailable(String service, int port) {
        Sleeper.sleep(5000, () -> {
            try {
                getEnvironment().getServiceHost(service, port);
                getEnvironment().getServicePort(service, port);
                return true;
            } catch (Exception e) {
                return false;
            }
        }, "Not started " + service + ":" + port);

    }

    protected CloseableHttpClient getHttpClient() {
        var custom = HttpClients.custom();
        var proxy = new HttpHost("http", proxyHost, proxyPort);
        var routePlanner = new DefaultProxyRoutePlanner(proxy);
        return custom.setRoutePlanner(routePlanner).build();
    }

    protected String httpGet(String path) {
        return new String(httpGetBinaryFile(path));
    }

    protected byte[] httpGetBinaryFile(String path) {
        try (var client = getHttpClient()) {
            var httpget = new HttpGet(path);
            var httpresponse = client.execute(httpget);

            var baos = new ByteArrayOutputStream();
            httpresponse.getEntity().writeTo(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void humanWait() {
        if (System.getenv("HUMAN_WAIT_MS") != null) {
            var humanWaitTime = Integer.parseInt(System.getenv("HUMAN_WAIT_MS"));
            Sleeper.sleep(humanWaitTime * 1000L);
        }
        takeSnapshot();
    }

    protected void takeSnapshot() {
        selenium.takeSnapShot();
    }

    public void tearDownAfterEachBase() {
        Utils.setCache("driver", null);
        Utils.setCache("js", null);
        selenium.takeMessageSnapshot("End of test");
        driver.quit();
        driver = null;
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
        proxyHost = getEnvironment().getServiceHost(tpmHost, 9000);
        proxyPort = getEnvironment().getServicePort(tpmHost, 9000);


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

    public void stopContainer(String host) throws Exception {
        var containerName = getEnvironment().getContainerByServiceName(host).get().getContainerInfo().getName().substring(1);
        var commandRunner = new CommandRunner(
                getProjectRoot(),
                "docker", "stop", containerName);
        commandRunner.run();
    }

    public void startContainer(String host) throws Exception {
        var containerName = getEnvironment().getContainerByServiceName(host).get().getContainerInfo().getName().substring(1);
        var commandRunner = new CommandRunner(
                getProjectRoot(),
                "docker", "start", containerName);
        commandRunner.run();
    }


    public boolean navigateTo(String url) {
        return getNavigation().navigateTo(url, true);
    }

    public boolean navigateTo(String url, boolean snapshot) {
        return getNavigation().navigateTo(url, snapshot);
    }

    public boolean check(BooleanSupplier supplier) {
        return check(2000, supplier);
    }

    public boolean check(int timoutms, BooleanSupplier supplier) {
        return Sleeper.sleepNoException(timoutms, supplier);
    }

    public String getPageSource() {
        return getDriver().getPageSource();
    }

    public boolean clickItem(String id) {
        return clickItem(2000, id);
    }

    protected WebElement findElementById(String id) {
        return findElementById(2000, id);
    }

    protected WebElement findElementByXPath(String id) {
        return findElementByXPath(2000, id);
    }

    protected WebElement findElementByXPath(int timeoutms, String id) {
        var element = new ObjectContainer<WebElement>();
        Sleeper.sleep(timeoutms, () -> {
            element.setObject(getDriver().findElement(By.xpath(id)));
            return element.getObject() != null;
        });
        return element.getObject();
    }

    protected List<WebElement> findElementsByXpath(String xpath) {
        return getDriver().findElements(By.xpath(xpath));
    }


    protected WebElement findElementById(int timeoutms, String id) {
        var element = new ObjectContainer<WebElement>();
        Sleeper.sleep(timeoutms, () -> {
            element.setObject(getDriver().findElement(By.id(id)));
            return element.getObject() != null;
        });
        return element.getObject();
    }

    public boolean clickItem(int timeoutms, String id) {
        var element = findElementById(timeoutms, id);
        var done = new ObjectContainer<>(false);

        Sleeper.sleep(timeoutms, () -> {
            var we = element;
            try {
                we.click();
                done.setObject(true);
                return true;
            } catch (Exception e) {
                return false;
            }
        });
        if (!done.getObject()) {
            var we = element;
            we.click();
            return true;
        } else {
            return true;
        }
    }

    protected void selectItem(String id, String value) {
        selectItem(2000, id, value);
    }

    protected void selectItem(int timeoutms, String id, String value) {
        var element = findElementById(timeoutms, id);
        selectItem(element, value);
    }

    protected void selectItem(WebElement element, String value) {
        Select select = new Select(element);
        select.selectByValue(value);
    }

    public void fillItem(String id, String data) {
        fillItem(2000, id, data);
    }

    public void fillItem(int timeoutms, String id, String data) {
        var element = findElementById(timeoutms, id);
        element.clear();
        element.sendKeys(data);
        getSelenium().takeSnapShot();
    }


    public void newTab(String id) {
        getSelenium().newTab(id);
        Sleeper.sleep(200);
    }


    private String getCurrentTab() {
        return getSelenium().getCurrentTab();
    }

    public void switchToTab(String id) {
        getSelenium().switchToTab(id);
        Sleeper.sleep(200);
    }

    public void executeScript(String script) {
        try {
            check(() -> !getPageSource().contains(script));
            ((JavascriptExecutor) getDriver()).executeScript(script);
            Sleeper.sleep(200);
            takeSnapshot();
        } catch (Exception e) {
            System.out.println("Script execution failed");
        }
    }

    protected void alertWhenHumanDriven(String message) {
        if(System.getenv("HUMAN_DRIVEN") != null) {
            ((JavascriptExecutor)getDriver()).executeScript("alert('" + message + "')");
            var alert = driver.switchTo().alert();
            while(alert != null) {
                try{
                    alert = driver.switchTo().alert();
                    Sleeper.sleep(200);
                }catch (Exception e) {
                    alert = null;
                }
            }
            //System.out.println("Human driven alert");
        }
    }

    protected void cleanBrowserCache() {
        driver.manage().deleteAllCookies();
        navigateTo("about:blank");

        var currentTab = getCurrentTab();
        if(!existsTab("settings")) {
            newTab("settings");
        }else{
            switchToTab("settings");
        }
        driver.get("chrome://settings/clearBrowserData");
        driver.findElement(By.xpath("//settings-ui")).sendKeys(Keys.ENTER);
        Sleeper.sleep(500);
        navigateTo("about:blank");
        switchToTab(currentTab);

    }

    private boolean existsTab(String id) {
        return getSelenium().existsTab(id);
    }


    public WebElement scrollFind(String id, long... extraLength) throws Exception {
        var js = (JavascriptExecutor) getDriver();
        var result = js.executeScript("return Math.max(" +
                "document.body.scrollHeight," +
                "document.body.offsetHeight," +
                "document.body.clientHeight," +
                "document.documentElement.scrollHeight," +
                "document.documentElement.offsetHeight," +
                "document.documentElement.clientHeight" +
                ");").toString();
        var length = Integer.parseInt(result);
        for (int i = 0; i < length; i += 100) {
            js.executeScript("window.scrollTo(0," + i + ")");
            var we = getDriver().findElements(By.id(id)).size();
            if (we == 0) {
                continue;
            }
            if (extraLength.length > 0) {
                js.executeScript("arguments[0].scrollIntoView(true);window.scrollBy(0,-100);", we);
                //js.executeScript("window.scrollTo(0," + (i+extraLength[0]) + ")");
            }
            return getDriver().findElement(By.id(id));
        }
        throw new RuntimeException("Unable to find item!");
    }
}
