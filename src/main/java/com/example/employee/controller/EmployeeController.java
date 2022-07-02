package com.example.employee.controller;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;


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
    public ResponseEntity<Object> getEmployeeById(@PathVariable String id) throws JsonMappingException, JsonProcessingException{
        try {    
            Employee employee = service.getEmp(id);
            if(employee == null){
                return new ResponseEntity<Object>("No employee with id: " + id, HttpStatus.BAD_REQUEST);
            }
            
            return new ResponseEntity<Object>(employee, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<Object>("Internal Server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getAllEmployees")
    public ResponseEntity<List<Employee>> getAllEmployees(){
        List<Employee> employees = service.getAll();
        return new ResponseEntity<List<Employee>>(employees, HttpStatus.OK);
    }

    @PutMapping("/updateEmployee")
    public ResponseEntity<Object> updateEmployee(@RequestBody Employee employee) throws JsonMappingException, JsonProcessingException{
        try {
            
        
            Employee updatedEmployee = service.updateEmployee(employee);
            if(updatedEmployee == null){
                return new ResponseEntity<Object>("No employee with id: " + employee.getId(), HttpStatus.BAD_REQUEST);    
            }
            
            return new ResponseEntity<Object>(updatedEmployee, HttpStatus.OK);
        } catch(Exception e) {
            e.printStackTrace();
            return new ResponseEntity<Object>("Internal Server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/deleteEmployee/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable String id){
        String status = service.deleteEmp(id);
        return new ResponseEntity<String>(status, HttpStatus.OK);
    }

    
    @PostMapping("/updateQueue")
    public ResponseEntity<Object> queue(@RequestBody Employee employee) throws IOException, TimeoutException, InterruptedException{
        try {
            System.out.println("[x] Requesting: " + employee);
            Employee updatedEmployee = (Employee) template.convertSendAndReceive(RabbitmqConfig.Exchange, RabbitmqConfig.RoutingKey, employee);
            System.out.println("updated emp: " + updatedEmployee);
            if(updatedEmployee == null){
                return new ResponseEntity<Object>("Bad data format or employee does not exist with given id: " + employee.getId(), HttpStatus.BAD_REQUEST);
            }
            
            return new ResponseEntity<Object>(updatedEmployee, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<Object>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
