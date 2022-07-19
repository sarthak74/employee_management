package com.example.employee.kafka;

import com.example.employee.entity.Employee;
import com.example.employee.repository.EmployeeRedisRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);
    @Autowired
    EmployeeRedisRepo redisRepo;

    @KafkaListener(topics = "secondTopic", groupId = "kafkaConsumer")
    public void consume(String employee){
        ObjectMapper mapper = new ObjectMapper();
        try {
            Employee employeeObject = mapper.readValue(employee, Employee.class);
            log.info("[.] Employee Management Got request: " + employeeObject);
            redisRepo.updateEmployee(employeeObject.getId(), employeeObject);
        } catch (Exception e) {
            e.getStackTrace();
            log.info("exception: " + e);
        }
    }
}
