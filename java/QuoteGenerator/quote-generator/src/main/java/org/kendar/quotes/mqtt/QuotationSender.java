package org.kendar.quotes.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

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
    private ObjectMapper mapper = new ObjectMapper();

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
        if(Math.abs(min-max)<2)max=min+2;
        return random.nextInt(Math.abs(min - max)) + Math.abs(min);
    }

    public void initialize(List<QuotationStatus> quotations) throws Exception {
        this.quotations = quotations;

        publisher = new MqttClient("tcp://"+hostName+":"+port, clientId);

        var options = new MqttConnectOptions();
        options.setAutomaticReconnect(automaticReconnect);
        options.setCleanSession(cleanSession);
        options.setConnectionTimeout(connectionTimeout);
        publisher.connect(options);
    }

    public void sendData() {
        for(var quotation : quotations) {
            var volume = randomValue(0, (int) limit(quotation.getVolume()));
            volume = randomValue(0,100)>50?-Math.abs(volume):Math.abs(volume);
            quotation.setVolume((int) Math.abs(volume+quotation.getVolume()));
            var value = randomValue(0, (int) limit(quotation.getPrice()));
            value = randomValue(0,100)>50?-Math.abs(value):Math.abs(value);
            quotation.setPrice(Math.abs(value+quotation.getPrice()));

            var quotationMessage = new QuotationMessage();
            quotationMessage.setSymbol(quotation.getSymbol());
            quotationMessage.setVolume((int)volume);
            quotationMessage.setBuy(Math.abs(value+0.02));
            quotationMessage.setSell(Math.min(Math.abs(value+0.02),Math.abs(value-0.02)));
            quotationMessage.setDate(Calendar.getInstance());

            try {
                var messageContent = mapper.writeValueAsBytes(quotationMessage);
                MqttMessage message = new MqttMessage(messageContent);
                message.setQos(qos);
                publisher.publish(topic, message);
            } catch (Exception e) {
                System.err.println("Error sending "+e.getMessage());
            }
        }

    }

    private double limit(double price) {
        if(price<10){
            return price/3;
        }else if(price<100){
            return price/20;
        }else if(price<1000){
            return price/30;
        }else{
            return price/100;
        }
    }
}
