package org.kendar.quotes;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.kendar.quotes.mqtt.QuotationSender;
import org.kendar.quotes.mqtt.QuotationStatus;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    public static AtomicBoolean running  = new AtomicBoolean(true);
    public static void main(String[] args) throws Exception {
        var properties = loadProperties();
        var httpPort = Integer.parseInt(properties.getProperty("http.port"));
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
        startStatusServer(httpPort);
        while(running.get()){
            qs.sendData();
            Thread.sleep(1000);
        }
    }

    private static void startStatusServer(int httpPort) throws Exception{
        var address = new InetSocketAddress(httpPort);
        var httpServer = HttpServer.create(address, 10);
        httpServer.createContext("/api/status", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                try {
                    var bytes = "OK".getBytes();
                    var outputStream = exchange.getResponseBody();
                    exchange.getResponseHeaders().add("Content-Type", "text/plain");
                    exchange.sendResponseHeaders(200, bytes.length);
                    outputStream.write(bytes);
                    outputStream.flush();
                    outputStream.close();
                } catch (Exception ex) {
                    System.err.println("Error responding "+ex.getMessage());
                    throw new RuntimeException(ex);
                }
            }
        });
        httpServer.start();
    }

    public static Properties loadProperties() throws Exception {
        InputStream inputStream;
        var prop = new Properties();
        File initialFile = Path.of("application.properties").toAbsolutePath().toFile();
        inputStream = new FileInputStream(initialFile);
        if (inputStream != null) {
            prop.load(inputStream);
        } else {
            throw new FileNotFoundException("property file 'application.properties' not found in the classpath");
        }
        return prop;
    }
}