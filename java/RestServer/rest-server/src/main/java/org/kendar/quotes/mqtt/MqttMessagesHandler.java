package org.kendar.quotes.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.kendar.quotes.data.Quotation;
import org.kendar.quotes.data.QuotationsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class MqttMessagesHandler {
    private final QuotationsRepository repository;
    private final IMqttClient queueClient;
    private final int qos;
    private final String topic;
    private final ObjectMapper objectMapper=new ObjectMapper();

    public MqttMessagesHandler(QuotationsRepository repository,
                               IMqttClient queueClient,
                               @Value("${messages.qos}") int qos,
                               @Value("${messages.topic}") String topic) {
        this.repository = repository;
        this.queueClient = queueClient;
        this.qos = qos;
        this.topic = topic;
    }

    @PostConstruct
    public void init() {
        queueClient.setCallback(new MqttCallback() {
            public void messageArrived(String topic, MqttMessage message) {
                var messageText = new String(message.getPayload());
                try {
                    var quotationMessage = objectMapper.readValue(messageText,QuotationMessage.class);
                    var quotation = new Quotation();
                    quotation.setBuy(quotationMessage.getBuy());
                    quotation.setSell(quotationMessage.getSell());
                    quotation.setDate(quotationMessage.getDate());
                    quotation.setSymbol(quotationMessage.getSymbol());
                    quotation.setVolume(quotationMessage.getVolume());
                    repository.save(quotation);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }

            public void connectionLost(Throwable cause) {
                System.err.println("connectionLost: " + cause.getMessage());
            }

            public void deliveryComplete(IMqttDeliveryToken token) {
                System.out.println("deliveryComplete: " + token.isComplete());
            }
        });
        try {
            queueClient.subscribe(topic, qos);
        } catch (MqttException cause) {
            System.err.println("unable to subscribe: " + cause.getMessage());
            throw new RuntimeException(cause);
        }
    }
}