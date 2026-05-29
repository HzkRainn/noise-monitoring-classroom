package com.noise.monitoring.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.noise.monitoring.dto.NoiseRequest;
import com.noise.monitoring.service.RealtimeNoiseProcessingService;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.integration.annotation.ServiceActivator;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import org.springframework.stereotype.Component;

@Component
public class MQTTSubscriber {

    @Autowired
    private RealtimeNoiseProcessingService realtimeService;

    // =========================================
    // JSON PARSER
    // =========================================

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    // =========================================
    // ANTI FLOOD
    // =========================================

    private long lastProcessedTime = 0;

    private static final long
            MIN_PROCESS_INTERVAL = 200;

    // =========================================
    // MQTT MESSAGE HANDLER
    // =========================================

    @ServiceActivator(
            inputChannel = "mqttInputChannel"
    )
    public void handleMessage(
            Message<?> message
    ) {

        long startTime =
                System.currentTimeMillis();

        try {

            System.out.println(
                    "\n================================="
            );

            System.out.println(
                    "MQTT MESSAGE RECEIVED"
            );

            // =====================================
            // MQTT TOPIC
            // =====================================

            MessageHeaders headers =
                    message.getHeaders();

            Object topic =
                    headers.get(
                            "mqtt_receivedTopic"
                    );

            System.out.println(
                    "TOPIC : "
                    + topic
            );

            // =====================================
            // PAYLOAD EXTRACTION
            // =====================================

            String payload =
                    String.valueOf(
                            message.getPayload()
                    );

            // =====================================
            // EMPTY PAYLOAD CHECK
            // =====================================

            if (
                    payload == null
                    ||
                    payload.isBlank()
                    ||
                    payload.equals("null")
            ) {

                System.out.println(
                        "INVALID PAYLOAD"
                );

                return;
            }

            System.out.println(
                    "RAW PAYLOAD : "
                    + payload
            );

            // =====================================
            // ANTI FLOOD
            // =====================================

            long now =
                    System.currentTimeMillis();

            if (
                    now - lastProcessedTime
                    <
                    MIN_PROCESS_INTERVAL
            ) {

                System.out.println(
                        "MESSAGE SKIPPED "
                        +
                        "(ANTI FLOOD)"
                );

                return;
            }

            lastProcessedTime = now;

            // =====================================
            // JSON PARSE
            // =====================================

            NoiseRequest request =
                    objectMapper.readValue(
                            payload,
                            NoiseRequest.class
                    );

            // =====================================
            // REQUEST VALIDATION
            // =====================================

            if (request == null) {

                System.out.println(
                        "REQUEST NULL"
                );

                return;
            }

            if (
                    request.getClassroomId()
                    == null
            ) {

                System.out.println(
                        "CLASSROOM ID NULL"
                );

                return;
            }

            // =====================================
            // DEBUG INFO
            // =====================================

            System.out.println(
                    "CLASSROOM ID : "
                    + request.getClassroomId()
            );

            System.out.println(
                    "DB LEVEL : "
                    + request.getDbLevel()
            );

            System.out.println(
                    "DOMINANT FREQUENCY : "
                    + request.getDominantFrequency()
            );

            System.out.println(
                    "VARIANCE : "
                    + request.getVariance()
            );

            System.out.println(
                    "SPIKE COUNT : "
                    + request.getSpikeCount()
            );

            // =====================================
            // REALTIME PIPELINE
            // =====================================

            realtimeService
                    .processRealtimeNoise(
                            request
                    );

            // =====================================
            // PROCESS TIME
            // =====================================

            long endTime =
                    System.currentTimeMillis();

            System.out.println(
                    "PROCESS TIME : "
                    +
                    (endTime - startTime)
                    +
                    " ms"
            );

            System.out.println(
                    "PROCESSING SUCCESS"
            );

            System.out.println(
                    "=================================\n"
            );

        } catch (Exception e) {

            System.out.println(
                    "MQTT PROCESSING ERROR"
            );

            System.out.println(
                    "ERROR MESSAGE : "
                    + e.getMessage()
            );

            e.printStackTrace();

            System.out.println(
                    "=================================\n"
            );
        }
    }
}