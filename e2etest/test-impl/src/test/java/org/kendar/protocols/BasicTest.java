package org.kendar.protocols;

import org.junit.jupiter.api.TestInfo;
import org.kendar.protocol.utils.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.FakeStrategy;
import org.testcontainers.containers.wait.strategy.PortWaitStrategy;

import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.function.BooleanSupplier;

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

    protected void humanWait(){
        //Sleeper.sleep(4000);
    }

    public static void tearDownAfterClassBase(){
        environment.stop();
    }
    public void tearDownAfterEachBase(){
        Utils.setCache("driver", null);
        Utils.setCache("js", null);
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
        toWaitFor = new HashMap<>();
        environment = new ComposeContainer(
                Path.of(getProjectRoot().toString(), "docker-compose-testcontainers.yml").toFile()
        );
        toWaitFor.put(tpmHost,8081);
        withExposedServiceHidden(tpmHost,5005);
        withExposedService(tpmHost,8081);
        withExposedService(tpmHost,9000);
        withExposedService(tpmHost,80);
        return environment;
    }

    protected static void startContainers() {
        environment.start();
        for(var item : toWaitFor.entrySet()){
            waitPortAvailable(item.getKey(),item.getValue());
        }
    }
    public static ComposeContainer withExposedService(String host,int ports) throws Exception {
        //environment.withExposedService(host, mainPort);
        environment.withExposedService(host, ports,
                new PortWaitStrategy().
                        forPorts(ports).
                        withStartupTimeout(Duration.ofSeconds(5)));
        toWaitFor.put(host,ports);
        return environment;
    }

    public static ComposeContainer withExposedServiceHidden(String host,int ports) throws Exception {
        //environment.withExposedService(host, mainPort);
        environment.withExposedService(host, ports,
                new FakeStrategy());
        toWaitFor.put(host,ports);
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

    public static void waitPortAvailable(String service, int port){
        Sleeper.sleep(5000,()->{
            try{
                var proxyHost = getEnvironment().getServiceHost(service, port);
                var proxyPort = getEnvironment().getServicePort(service, port);
                return true;
            }catch (Exception e){
                return false;
            }
        },"Not started "+service+":"+port);

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
        var proxyHost = getEnvironment().getServiceHost(tpmHost, 9000);
        var proxyPort = getEnvironment().getServicePort(tpmHost, 9000);

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



    public boolean navigateTo(String url){
        return getNavigation().navigateTo(url,true);
    }
    public boolean navigateTo(String url,boolean snapshot){
        return getNavigation().navigateTo(url,snapshot);
    }

    public boolean check(BooleanSupplier supplier){
        return check(2000,supplier);
    }

    public boolean check(int timoutms, BooleanSupplier supplier){
        return Sleeper.sleepNoException(timoutms,supplier);
    }

    public String getPageSource(){
        return getDriver().getPageSource();
    }

    public boolean clickItem(String id){
        return clickItem(2000,id);
    }

    protected WebElement findElementById(String id){
        return findElementById(2000,id);
    }

    protected WebElement findElementById(int timeoutms,String id){
        var element = new ObjectContainer<WebElement>();
        Sleeper.sleep(timeoutms,()-> {
            element.setObject(getDriver().findElement(By.id(id)));
            return element.getObject()!=null;
        });
        return element.getObject();
    }
    public boolean clickItem(int timeoutms, String id){
        var element = findElementById(timeoutms,id);
        var done = new ObjectContainer<>(false);

        Sleeper.sleep(timeoutms,()-> {
            var we = element;
            try {
                we.click();
                done.setObject(true);
                return true;
            }catch (Exception e){
                return false;
            }
        });
        if(!done.getObject()){
            var we = element;
            we.click();
            return true;
        }else{
            return true;
        }
    }

    protected void selectItem(String id, String value) {
        selectItem(2000,id,value);
    }
    protected void selectItem(int timeoutms,String id, String value) {
        var element = findElementById(timeoutms,id);
        selectItem(element,value);
    }

    protected void selectItem(WebElement element, String value) {
        Select select = new Select(element);
        select.selectByValue(value);
    }

    public void fillItem(String id, String data) {
        fillItem(2000,id,data);
    }

    public void fillItem(int timeoutms,String id, String data) {
        var element = findElementById(timeoutms,id);
        element.clear();
        element.sendKeys(data);
        getSelenium().takeSnapShot();
    }
}
