package org.kendar.protocol.utils;

import org.openqa.selenium.WebDriver;

public class NavigationUtils {
    private final SeleniumIntegration seleniumIntegration;

    public NavigationUtils(SeleniumIntegration seleniumIntegration) {
        this.seleniumIntegration = seleniumIntegration;
    }
    public boolean navigateTo(String url) {
        return this.navigateTo(url,true);
    }

    public boolean navigateTo(String url,boolean snapshot) {
        var driver = (WebDriver) Utils.getCache("driver");
        var current = driver.getCurrentUrl();
        if (current.equalsIgnoreCase(url)) {
            Sleeper.sleep(1000);
            if(snapshot)seleniumIntegration.takeSnapShot();
            return true;
        }
        driver.get(url);
        if(snapshot)seleniumIntegration.takeSnapShot();
        return false;
    }
}
