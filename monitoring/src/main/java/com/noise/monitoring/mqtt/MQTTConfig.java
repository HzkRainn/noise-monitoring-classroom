package com.noise.monitoring.mqtt;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.integration.channel.DirectChannel;

import org.springframework.integration.core.MessageProducer;

import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;

import org.springframework.integration.mqtt.core.MqttPahoClientFactory;

import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;

import org.springframework.messaging.MessageChannel;

@Configuration
public class MQTTConfig {

    // =====================================================
    // MQTT CONFIGURATION
    // =====================================================

    public static final String MQTT_BROKER =
            "tcp://localhost:1883";

    public static final String MQTT_CLIENT_ID =
            "spring-noise-monitor";

    /*
     =========================================
     FINAL STANDARD TOPIC
     =========================================
    */
    public static final String MQTT_TOPIC =
            "classroom/noise";

    // =====================================================
    // MQTT CLIENT FACTORY
    // =====================================================

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {

        DefaultMqttPahoClientFactory factory =
                new DefaultMqttPahoClientFactory();

        MqttConnectOptions options =
                new MqttConnectOptions();

        // =============================================
        // BROKER
        // =============================================

        options.setServerURIs(
                new String[]{
                        MQTT_BROKER
                }
        );

        // =============================================
        // STABILITY CONFIG
        // =============================================

        options.setAutomaticReconnect(true);

        options.setCleanSession(false);

        options.setConnectionTimeout(10);

        options.setKeepAliveInterval(20);

        options.setMaxInflight(1000);

        // =============================================
        // DEBUG
        // =============================================

        System.out.println(
                "\n================================="
        );

        System.out.println(
                "MQTT CONFIG INITIALIZED"
        );

        System.out.println(
                "BROKER : " + MQTT_BROKER
        );

        System.out.println(
                "TOPIC : " + MQTT_TOPIC
        );

        System.out.println(
                "CLIENT ID : " + MQTT_CLIENT_ID
        );

        System.out.println(
                "=================================\n"
        );

        factory.setConnectionOptions(options);

        return factory;
    }

    // =====================================================
    // INPUT CHANNEL
    // =====================================================

    @Bean
    public MessageChannel mqttInputChannel() {

        return new DirectChannel();
    }

    // =====================================================
    // MQTT SUBSCRIBER
    // =====================================================

    @Bean
    public MessageProducer inbound() {

        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(

                        MQTT_CLIENT_ID,

                        mqttClientFactory(),

                        MQTT_TOPIC
                );

        // =============================================
        // SUBSCRIBER CONFIG
        // =============================================

        adapter.setCompletionTimeout(5000);

        adapter.setQos(1);

        adapter.setOutputChannel(
                mqttInputChannel()
        );

        // =============================================
        // DEBUG
        // =============================================

        System.out.println(
                "MQTT SUBSCRIBER READY"
        );

        return adapter;
    }
}