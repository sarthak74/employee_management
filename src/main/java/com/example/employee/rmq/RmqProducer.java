package com.example.employee.rmq;

import com.example.employee.config.RabbitmqConfig;
import com.example.employee.entity.Employee;
import com.example.employee.repository.EmployeeRedisRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RmqProducer {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final Logger log = LoggerFactory.getLogger(EmployeeRedisRepo.class);

    public Employee sendMessage(Employee employee){
        try {
            log.info("[x] Requesting: " + employee);
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(employee);
            String updatedEmployee = (String) rabbitTemplate.convertSendAndReceive(RabbitmqConfig.Exchange, RabbitmqConfig.RoutingKey, json);
            Employee updatedEmployeeObject = mapper.readValue(updatedEmployee, Employee.class);
            log.info("updated emp: " + updatedEmployeeObject);
            return updatedEmployeeObject;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
