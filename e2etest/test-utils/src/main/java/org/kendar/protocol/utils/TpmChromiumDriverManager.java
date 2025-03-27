package org.kendar.protocol.utils;

import io.github.bonigarcia.wdm.managers.ChromiumDriverManager;
import org.openqa.selenium.Capabilities;

public class TpmChromiumDriverManager extends ChromiumDriverManager {
    public Capabilities retrieveCapabilities() {
        return getCapabilities();
    }
}
