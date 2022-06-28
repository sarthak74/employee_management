package com.example.employee.controller;

import java.util.HashMap;
import java.util.List;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.employee.config.RabbitmqConfig;
import com.example.employee.entity.Employee;

import com.example.employee.service.EmployeeService;


@RestController
public class EmployeeController {
    @Autowired
    private EmployeeService service;

    @Autowired
    private RabbitTemplate template;


    @PostMapping("/addEmployee")
    public ResponseEntity<Employee> addEmployee(@RequestBody Employee employee){
        Employee addedEmployee = service.save(employee);
        return new ResponseEntity<Employee>(addedEmployee, HttpStatus.OK);
    }

    @PostMapping("/addMultipleEmployees")
    public ResponseEntity<List<Employee>> addMultipleEmployees(@RequestBody List<Employee> employees){
        List<Employee> addedEmployees = service.saveAll(employees);
        return new ResponseEntity<List<Employee>>(addedEmployees, HttpStatus.OK);
    }

    @GetMapping("/getEmployee/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable int id){
        Employee employee = service.getEmp(id);
        return new ResponseEntity<Employee>(employee, HttpStatus.OK);
    }

    @GetMapping("/getAllEmployees")
    public ResponseEntity<List<Employee>> getAllEmployees(){
        List<Employee> employees = service.getAll();
        return new ResponseEntity<List<Employee>>(employees, HttpStatus.OK);
    }

    @PutMapping("/updateEmployee")
    public ResponseEntity<String> updateEmployee(@RequestBody Employee employee){
        System.out.println("updating employee: " + employee);
        Employee prev_employee = service.getEmp(employee.getId());
        System.out.println("prev_emp: "+ prev_employee);
        if(prev_employee == null){
            return new ResponseEntity<String>("No employee with id: " + employee.getId(), HttpStatus.BAD_REQUEST);
        }
        
        template.convertSendAndReceive(RabbitmqConfig.Exchange, RabbitmqConfig.RoutingKey, employee);
        
        return new ResponseEntity<String>("Employee updated successfully", HttpStatus.OK);
    }

    @DeleteMapping("/deleteEmployee/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable int id){
        String status = service.deleteEmp(id);
        return new ResponseEntity<String>(status, HttpStatus.OK);
    }

    @PostMapping("/queue")
    public Employee queue(@RequestBody Employee employee){
        System.out.println("queuing: " + employee.toString());
        Message newMessage = MessageBuilder.withBody(employee.toString().getBytes()).build();
        Employee newEmployee =  (Employee)template.convertSendAndReceive(RabbitmqConfig.Exchange, RabbitmqConfig.RoutingKey, employee);
        // String response = "";
        System.out.println("newEmp: " + newEmployee);
        // if(result != null){
        //     String id = newMessage.getMessageProperties().getCorrelationId();

        //     System.out.println("rec id:" + id);
        //     HashMap<String, Object> headers = (HashMap<String, Object>) result.getMessageProperties().getHeaders();
        //     System.out.println("heads: " + headers);
        //     String msgId = (String) headers.get("spring_returned_message_correlation");
            
        //     if(msgId.equals(id)){
        //         response = new String(result.getBody());
        //     }

        // }
        // System.out.println(("resp: " + response));

        return newEmployee;
    }
}
