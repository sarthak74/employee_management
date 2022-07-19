package com.example.employee.kafka;

import com.example.employee.entity.Employee;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private ObjectMapper mapper = new ObjectMapper();;

    public String getMessage(Employee employee){
        try {
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(employee);
            return json;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public Boolean sendMessage(Employee employee){
        try {
            String message = getMessage(employee);
            log.info(String.format("[x] Sending Kafka request. Msg: " + message));
            kafkaTemplate.send("firstTopic", message);
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
