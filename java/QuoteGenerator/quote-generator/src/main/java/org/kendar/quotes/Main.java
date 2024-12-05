package org.kendar.quotes;

import org.kendar.quotes.mqtt.QuotationSender;
import org.kendar.quotes.mqtt.QuotationStatus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws Exception {
        var properties = loadProperties();
        var qs = new QuotationSender(properties);
        var quotations = List.of(
                new QuotationStatus("MSFT",qs.randomValue(0,100),(int)qs.randomValue(10,1000)),
                new QuotationStatus("NVDA",qs.randomValue(0,100),(int)qs.randomValue(10,1000)),
                new QuotationStatus("GOOGL",qs.randomValue(0,100),(int)qs.randomValue(10,1000)),
                new QuotationStatus("MSTR",qs.randomValue(0,100),(int)qs.randomValue(10,1000)),
                new QuotationStatus("PLTR",qs.randomValue(0,100),(int)qs.randomValue(10,1000)),
                new QuotationStatus("AMZN",qs.randomValue(0,100),(int)qs.randomValue(10,1000)),
                new QuotationStatus("MRVL",qs.randomValue(0,100),(int)qs.randomValue(10,1000)),
                new QuotationStatus("META",qs.randomValue(0,100),(int)qs.randomValue(10,1000))
        );
        qs.initialize(quotations);
        while(true){
            qs.sendData();
            Thread.sleep(1000);
        }
    }

    public static Properties loadProperties() throws Exception {
        InputStream inputStream;
        // Class path is found under WEB-INF/classes
        var prop = new Properties();
        File initialFile = Path.of("application.properties").toAbsolutePath().toFile();
        inputStream = new FileInputStream(initialFile);
        // read the file
        if (inputStream != null) {
            prop.load(inputStream);
        } else {
            throw new FileNotFoundException("property file 'application.properties' not found in the classpath");
        }

        // get the property value and print it out
        return prop;
    }
}