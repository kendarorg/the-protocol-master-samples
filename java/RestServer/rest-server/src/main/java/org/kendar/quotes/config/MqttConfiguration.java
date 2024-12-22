package org.kendar.quotes.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "mqtt")
    public MqttConnectOptions mqttConnectOptions() {
        return new MqttConnectOptions();

    }

    @Bean
    public MqttClientFactory mqttClient(@Value("${mqtt.clientId}") String clientId,
                                        @Value("${mqtt.hostname}") String hostname,
                                        @Value("${mqtt.port}") int port,
                                        MqttConnectOptions options) {
        return new MqttClientFactory("tcp://" + hostname + ":" + port, clientId, options);
    }
}
