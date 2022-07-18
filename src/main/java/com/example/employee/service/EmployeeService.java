package com.example.employee.service;

import java.util.*;
import java.util.stream.Collectors;

import com.example.employee.kafka.KafkaProducer;
import com.example.employee.repository.EmployeeRedisRepo;
import com.example.employee.rmq.RmqProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.employee.entity.Employee;
import com.example.employee.repository.EmployeeRepository;

@Service
public class EmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeRedisRepo.class);
    
    @Autowired
    private EmployeeRepository repo;

    @Autowired
    private RmqProducer rmqProducer;

    @Autowired
    private EmployeeRedisRepo redisService;

    @Autowired
    private KafkaProducer kafkaProducer;

    public Employee save(Employee employee){
        log.info("emp service: " + employee);
        Employee savedEmployee = repo.save(employee);
        List<Employee> newEmployeeList = new ArrayList<Employee>();
        newEmployeeList.add(employee);
        redisService.setAllEmployees(newEmployeeList);
        return savedEmployee;
    }

    public List<Employee> saveAll(List<Employee> employees){
        redisService.setAllEmployees(employees);
        return repo.saveAll(employees);
    }

    public List<Employee> getAll(){
        Map<String, Employee> employees = redisService.getAllEmployees();
        if (employees == null || employees.isEmpty()) {
            log.info("getting all emps from db, cache don't have it");
            List<Employee> allEmployees = repo.findAllEmployees();
            redisService.setAllEmployees(allEmployees);
            return allEmployees;
        } else {
            log.info("got all emps from cache");
            List<Employee> allEmployees = employees.values().stream()
                    .filter(employee -> employee.isDeleted() == false)
                    .collect(Collectors.toList());
            return allEmployees;
        }
    }

    public Employee getEmp(String id) {
        try {
            Employee employee = redisService.getEmployee(id);
            if(employee == null){
                log.info("getting emp: " + id + " from db, cache don't have it");
                employee = repo.findById(id).get();
                redisService.setAnEmployeeToCache(employee.getId(), employee);
            } else log.info("got emp: " + id + " from cache");
            if (employee == null) return null;
            if (employee.isDeleted()) return null;
            return employee;
        } catch (Exception e){
            return null;
        }
    }

    public Employee updateEmployee(Employee employeeUpdatedData){
        log.info("updating emp id: " + employeeUpdatedData.getId());
        String id = employeeUpdatedData.getId();
        Employee employee_to_be_updated = redisService.getEmployee(id);

        if(employee_to_be_updated == null){
            log.info("getting emp: " + id + " from db, cache don't have it");
            employee_to_be_updated = repo.findById(id).get();
        } else log.info("got emp: " + id + " from cache");

        if(employee_to_be_updated == null) return null;
        if(employee_to_be_updated.isDeleted() == true) return null;

        employee_to_be_updated = setEmployeeFields(employee_to_be_updated, employeeUpdatedData);
        repo.save(employee_to_be_updated);
        redisService.updateEmployee(id, employee_to_be_updated);
        return employee_to_be_updated;
    }

    public Employee updateEmployeeUsingQueue(Employee employeeUpdatedData){
        try {
            Employee updatedEmployeeObject = rmqProducer.sendMessage(employeeUpdatedData);
            redisService.updateEmployee(employeeUpdatedData.getId(), updatedEmployeeObject);
            return updatedEmployeeObject;
        } catch (Exception e) {
            e.printStackTrace();
            // below is a way to check about exception in controller class while sending response back to controller class
            employeeUpdatedData.setId("false");
            return employeeUpdatedData;
        }

    }

    public Employee updateEmployeeUsingKafka(Employee employee) {
        kafkaProducer.sendMessage(employee);
        return employee;
    }

    public Boolean deleteEmp(String id){
        Employee prev_employee = redisService.getEmployee(id);
        if(prev_employee == null){
            log.info("getting emp: " + id + " from db, cache don't have it");
            prev_employee = repo.findById(id).get();
        } else log.info("got emp: " + id + " from cache");
        if(prev_employee != null){
            if(prev_employee.isDeleted() == true) return false;
            prev_employee.setDeleted(true);
            repo.save(prev_employee);
            redisService.deleteKey(id);
            return true;
        }
        return false;
    }

    public Employee setEmployeeFields(Employee employeeToBeUpdated, Employee newEmployee){
        if(newEmployee.getName() != null) employeeToBeUpdated.setName(newEmployee.getName());
        if(newEmployee.getPod() != null) employeeToBeUpdated.setPod(newEmployee.getPod());
        if(newEmployee.getContact() != null) employeeToBeUpdated.setContact(newEmployee.getContact());
        if(newEmployee.getAge() != null) employeeToBeUpdated.setAge(newEmployee.getAge());
        return employeeToBeUpdated;
    }
}