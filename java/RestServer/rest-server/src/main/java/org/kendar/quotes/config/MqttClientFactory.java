package org.kendar.quotes.config;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.kendar.quotes.mqtt.NullMqtt;

public class MqttClientFactory {
    private final String connectionString;
    private final String clientId;
    private final MqttConnectOptions options;

    public MqttClientFactory(String connectionString, String clientId, MqttConnectOptions options) {

        this.connectionString = connectionString;
        this.clientId = clientId;
        this.options = options;
    }

    public IMqttClient connect() throws MqttException {
        if (clientId == null || clientId.isEmpty()) {
            return new NullMqtt();
        }
        IMqttClient mqttClient =
                new MqttClient(connectionString, clientId);
        mqttClient.connect(options);
        return mqttClient;

    }
}
