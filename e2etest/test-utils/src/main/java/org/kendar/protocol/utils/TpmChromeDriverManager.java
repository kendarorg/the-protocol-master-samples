package org.kendar.protocol.utils;

import io.github.bonigarcia.wdm.managers.ChromeDriverManager;
import org.openqa.selenium.Capabilities;

public class TpmChromeDriverManager extends ChromeDriverManager {
    public Capabilities retrieveCapabilities() {
        return getCapabilities();
    }
}
