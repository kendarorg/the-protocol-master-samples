package org.kendar.protocol.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.managers.ChromeDriverManager;
import io.github.bonigarcia.wdm.versions.VersionDetector;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class SeleniumIntegration {
    private final Path rootPath;
    private final String proxyHost;
    private final int proxyPort;
    private ChromeDriver driver;
    private JavascriptExecutor js;
    private Map<String,String> windowHandles = new HashMap<>();
    private String currentTab;

    public boolean navigateTo(String url) {
        return this.navigateTo(url,true);
    }

    public boolean navigateTo(String url,boolean snapshot) {
        var driver = (WebDriver) Utils.getCache("driver");
        var current = driver.getCurrentUrl();
        if (current.equalsIgnoreCase(url)) {
            Sleeper.sleep(1000);
            if(!getCurrentTab().equals("settings")) {
                if(snapshot)takeSnapShot();
            }
            return true;
        }
        driver.get(url);
        if(!getCurrentTab().equals("settings")) {
            if(snapshot)takeSnapShot();
        }
        return false;
    }

    public SeleniumIntegration(Path rootPath, String proxyHost, int proxyPort) {
        this.rootPath = rootPath;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    private void setupSize(ChromeDriver driver) {
        driver.manage().window().setSize(new Dimension(1366, 900));
    }

    private String retrieveBrowserVersion() {
        ChromeDriverManager.getInstance().setup();
        var versionDetector = new VersionDetector(ChromeDriverManager.getInstance().config(), null);
        var version = Integer.parseInt(versionDetector.getBrowserVersionFromTheShell("chrome").get());
        var available = ChromeDriverManager.getInstance().getDriverVersions().stream().
                map(v -> Integer.parseInt(v.split("\\.")[0])).sorted().distinct().collect(Collectors.toList());
        var matching = available.get(available.size() - 1);
        if (available.stream().anyMatch(v -> v == (version))) {
            matching = version;
        }
        return matching.toString();
    }

    public void seleniumInitialized() throws Exception {
        windowHandles.clear();
        //ChromeDriverManager.getInstance().setup();
        //var chromeExecutable = SeleniumBase.findchrome();

        Proxy proxy = new Proxy();
        proxy.setHttpProxy(proxyHost+":"+proxyPort);
        proxy.setProxyType(Proxy.ProxyType.MANUAL);
        //DesiredCapabilities desired = new DesiredCapabilities();
        var options = new ChromeOptions();
        options.setBrowserVersion(retrieveBrowserVersion());
        options.setProxy(proxy);
        options.setAcceptInsecureCerts(true);
        options.addArguments("--remote-allow-origins=*");
        //options.addArguments("--disable-dev-shm-usage");
        //options.addArguments("disable-infobars"); // disabling infobars
        //options.addArguments("--disable-extensions"); // disabling extensions
        options.addArguments("--disable-gpu"); // applicable to windows os only
        options.addArguments("--disable-dev-shm-usage"); // overcome limited resource problems
        options.addArguments("--no-sandbox"); // Bypass OS security model
        options.addArguments("--disk-cache-size=0");//Disable cache
        if(System.getenv("HUMAN_DRIVEN") == null && System.getenv("RUN_VISIBLE")==null) {
            options.addArguments("--headless");//Disable cache
        }


        driver = (ChromeDriver) WebDriverManager
                .chromedriver()
                .capabilities(options)
                .clearDriverCache()
                .clearResolutionCache()
                .create();


        //driver.manage().deleteAllCookies();


        js = (JavascriptExecutor) driver;
        Utils.setCache("driver", driver);
        Utils.setCache("js", js);
        setupSize(driver);
        windowHandles.put("main",driver.getWindowHandle());
        currentTab="main";


    }

    public void newTab(String id) {
        driver.switchTo().newWindow(WindowType.TAB);
        windowHandles.put(id,driver.getWindowHandle());
        currentTab=id;
    }

    public void switchToTab(String id) {
        driver.switchTo().window(windowHandles.get(id));
        currentTab=id;
    }

    public void resettingDriver() throws Exception {
        if(driver!=null)driver.quit();
        Utils.setCache("driver", null);
        Utils.setCache("js", null);
        seleniumInitialized();
    }


    public void quitSelenium() throws Exception {
        driver.quit();
        Utils.setCache("driver", null);
        Utils.setCache("js", null);
        Sleeper.sleep(1000);
        takeMessageSnapshot("End of test");
    }

    private int counter=0;

    public void takeSnapShot() {

        try {
            if(driver.getCurrentUrl().startsWith("about:")) {
                return;
            }
            var dest = rootPath;
            if (!Files.exists(dest)) {
                rootPath.toFile().mkdirs();
            }
            counter++;
            TakesScreenshot scrShot = ((TakesScreenshot) Utils.getCache("driver"));
            File srcFile = scrShot.getScreenshotAs(OutputType.FILE);
            var destFilePath = Path.of(rootPath.toString(), "snap_" + String.format("%03d", counter) + ".png");
            File destFile = new File(destFilePath.toAbsolutePath().toString());
            FileUtils.copyFile(srcFile, destFile);
            Files.delete(srcFile.getAbsoluteFile().toPath());
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    public void takeMessageSnapshot(String text) {
        try {
            var dest = rootPath;
            if (!Files.exists(dest)) {
                rootPath.toFile().mkdirs();
            }

            counter++;
            var destFilePath = Path.of(rootPath.toString(), "snap_" + String.format("%03d", counter) + ".png");

            var img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            var g2d = img.createGraphics();
            var font = new Font("Arial", Font.PLAIN, 48);
            g2d.setFont(font);
            FontMetrics fm = g2d.getFontMetrics();
            int width = 1024;//fm.stringWidth(text);
            int height = 768;//fm.getHeight();
            g2d.dispose();

            img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            g2d = img.createGraphics();
            g2d.setBackground(Color.WHITE);
            g2d.clearRect(0, 0, width, height);
            g2d.setFont(font);
            fm = g2d.getFontMetrics();
            g2d.setColor(Color.BLACK);
            g2d.drawString(text, 0 + 5, 5 + fm.getAscent());
            g2d.dispose();

            ImageIO.write(img, "png", new File(destFilePath.toAbsolutePath().toString()));
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    public String getCurrentTab() {
        return currentTab;
    }

    public boolean existsTab(String id) {
        return windowHandles.containsKey(id);
    }

    public void clearStatus() {
        driver.manage().deleteAllCookies();
    }
}
