package com.example.employee.kafka;

import com.example.employee.entity.Employee;
import com.example.employee.repository.EmployeeRedisRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    @Autowired
    EmployeeRedisRepo redisRepo;

    @KafkaListener(topics = "secondTopic", groupId = "kafkaConsumer")
    public void consume(String employee){
        ObjectMapper mapper = new ObjectMapper();
        try {
            Employee employeeObject = mapper.readValue(employee, Employee.class);
            System.out.println("[.] Employee Management Got request: " + employeeObject);
            redisRepo.updateEmployee(employeeObject.getId(), employeeObject);
        } catch (Exception e) {
            e.getStackTrace();
            System.out.println("exception: " + e);
        }
    }
}
