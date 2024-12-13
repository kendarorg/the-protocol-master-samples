package org.kendar.quotes.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class QuotationSender {
    private final int port;
    private final int connectionTimeout;
    private final String hostName;
    private final boolean cleanSession;
    private final boolean automaticReconnect;
    private final String clientId;
    private final Random random;
    private final String topic;
    private final int qos;
    private List<QuotationStatus> quotations;
    private MqttClient publisher;
    private final ObjectMapper mapper = new ObjectMapper();

    public QuotationSender(Properties properties) {
        port = Integer.parseInt(properties.getProperty("mqtt.port"));
        connectionTimeout = Integer.parseInt(properties.getProperty("mqtt.connectionTimeout"));
        hostName = properties.getProperty("mqtt.hostname");
        cleanSession = Boolean.parseBoolean(properties.getProperty("mqtt.cleanSession"));
        automaticReconnect = Boolean.parseBoolean(properties.getProperty("mqtt.automaticReconnect"));
        clientId = properties.getProperty("mqtt.clientId");
        topic = properties.getProperty("messages.topic");
        qos = Integer.parseInt(properties.getProperty("messages.qos"));
        random = new Random();
    }

    public double randomValue(int min, int max) {
        if (Math.abs(min - max) < 2) max = min + 2;
        return random.nextInt(Math.abs(min - max)) + Math.abs(min);
    }

    public void initialize(List<QuotationStatus> quotations) throws Exception {
        this.quotations = quotations;

        publisher = new MqttClient("tcp://" + hostName + ":" + port, clientId);

        var options = new MqttConnectOptions();
        options.setAutomaticReconnect(automaticReconnect);
        options.setCleanSession(cleanSession);
        options.setConnectionTimeout(connectionTimeout);
        publisher.connect(options);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        var bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public void sendData() {
        var volatility = 0.2;
        System.err.println("Sending");
        for (var quotation : quotations) {

            try {
                var old_price = quotation.getPrice();
                double rnd = randomValue(0, 100) / 100; // generate number, 0 <= x < 1.0
                // fmt.Printf("rnd %v ", rnd)
                var change_percent = 2 * volatility * rnd;
                // fmt.Printf("change_percent %v\n", change_percent)
                if (change_percent > volatility) {
                    change_percent = change_percent - (2 * volatility);
                }
                var change_amount = quotation.getPrice() * change_percent;
                quotation.setPrice(old_price + change_amount);
                quotation.setVolume((int) randomValue(1, 10000));

                var quotationMessage = new QuotationMessage();
                quotationMessage.setSymbol(quotation.getSymbol());
                quotationMessage.setVolume(quotation.getVolume());
                quotationMessage.setPrice(round(quotation.getPrice(),3));
                quotationMessage.setDate(Calendar.getInstance());

                var messageContent = mapper.writeValueAsBytes(quotationMessage);
                MqttMessage message = new MqttMessage(messageContent);
                message.setQos(qos);
                publisher.publish(topic, message);
            } catch (Exception e) {
                System.err.println("Error sending " + e.getMessage());
            }
        }
        System.err.println("Sent");

    }

    private double limit(double price) {
        if (price < 10) {
            return price / 3;
        } else if (price < 100) {
            return price / 20;
        } else if (price < 1000) {
            return price / 30;
        } else {
            return price / 100;
        }
    }
}
