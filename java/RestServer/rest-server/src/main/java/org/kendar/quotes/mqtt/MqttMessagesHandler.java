package org.kendar.quotes.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.kendar.quotes.config.MqttClientFactory;
import org.kendar.quotes.data.Quotation;
import org.kendar.quotes.data.QuotationsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class MqttMessagesHandler {
    private final QuotationsRepository repository;
    private final MqttClientFactory queueClient;
    private final int qos;
    private final String topic;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MqttMessagesHandler(QuotationsRepository repository,
                               MqttClientFactory queueClient,
                               @Value("${messages.qos}") int qos,
                               @Value("${messages.topic}") String topic) {
        this.repository = repository;
        this.queueClient = queueClient;
        this.qos = qos;
        this.topic = topic;
    }

    private MqttCallback connect(IMqttClient connection) {
        return new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                try {
                    connection.subscribe(topic, qos);
                } catch (MqttException e) {
                    throw new RuntimeException("UNABLE TO SUBSCRIBE", e);
                }
            }

            public void messageArrived(String topic, MqttMessage message) {
                var messageText = new String(message.getPayload());
                System.out.println("MESSAGE ARRIVED: " + messageText);
                try {
                    var quotationMessage = objectMapper.readValue(messageText, QuotationMessage.class);
                    var quotation = new Quotation();
                    quotation.setPrice(quotationMessage.getPrice());
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
                //for (var i = 0; i < 3; i++) {
                try {
                    var connectionInt = queueClient.connect();
                    connectionInt.setCallback(connect(connectionInt));
                    connectionInt.subscribe(topic, qos);
                    System.err.println("SUBSCRIBED POST RECONNECT");
                    connection.close();
                } catch (Exception e) {
                    System.err.println("SUBSCRIBED POST RECONNECT" + e.getMessage());
                }
                //}
            }

            public void deliveryComplete(IMqttDeliveryToken token) {
                System.out.println("deliveryComplete: " + token.isComplete());
            }
        };
    }


    @PostConstruct
    public void init() throws MqttException {
        var connection = queueClient.connect();
        connection.setCallback(connect(connection));
        connection.subscribe(topic, qos);
        System.err.println("SUBSCRIBED");
    }
}
