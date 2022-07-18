package com.example.employee.kafka;

import com.example.employee.entity.Employee;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.Properties;

@Service
public class KafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
    private KafkaTemplate<String, String> kafkaTemplate;

    private ObjectMapper mapper = new ObjectMapper();;

    public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    public String getMessage(Employee employee){
        try {
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(employee);
            return json;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public void sendMessage(Employee employee){
        String message = getMessage(employee);
        log.info(String.format("[x] Sending Kafka request. Msg: " + message));
        kafkaTemplate.send("firstTopic", message).addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onSuccess(final SendResult<String, String> message) {
                log.info("Message sent successfully\nMetadata: " + message.getRecordMetadata());
            }
            @Override
            public void onFailure(final Throwable throwable) {
                log.info("Message sending failed: " + message, throwable);
            }
        });
    }
}
