package com.example.employee.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import com.example.employee.config.RabbitmqConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.example.employee.entity.Employee;
import com.example.employee.repository.EmployeeRepository;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository repo;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private EmployeeRedisService redisService;


    public Employee save(Employee employee){
        System.out.println("emp service: " + employee);
        Employee savedEmployee = repo.save(employee);
        redisService.setEmployee(savedEmployee.getId(), savedEmployee);
        return savedEmployee;
    }

    public List<Employee> saveAll(List<Employee> employees){
        redisService.setAllEmployees(employees);
        return repo.saveAll(employees);
    }

    public List<Employee> getAll(){
        Map<String, Employee> employees = redisService.getAllEmployees();
        if (employees == null || employees.isEmpty()) {
            System.out.println("getting all emps from db, cache don't have it");
            List<Employee> allEmployees = repo.findAllEmployees();
            redisService.setAllEmployees(allEmployees);
            return allEmployees;
        } else {
            System.out.println("got all emps from cache");
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
                System.out.println("getting emp: " + id + " from db, cache don't have it");
                employee = repo.findById(id).get();
                redisService.setEmployee(id, employee);
            } else {
                System.out.println("got emp: " + id + " from cache");
            }
            if (employee == null) return null;
            if (employee.isDeleted()) return null;
            return employee;
        } catch (Exception e){
            return null;
        }
    }

    public Employee updateEmployee(Employee employee){
        System.out.println("updating emp id: " + employee.getId());
        String id = employee.getId();

        Employee prev_employee = redisService.getEmployee(id);
        if(prev_employee == null){
            System.out.println("getting emp: " + id + " from db, cache don't have it");
            prev_employee = repo.findById(id).get();
            redisService.setEmployee(id, prev_employee);
        } else {
            System.out.println("got emp: " + id + " from cache");
        }

        if(prev_employee == null){
            return null;
        }

        if(prev_employee.isDeleted() == true) {
            return null;
        }

        prev_employee.setName(employee.getName());
        prev_employee.setPod(employee.getPod());
        prev_employee.setContact(employee.getContact());
        prev_employee.setAge(employee.getAge());

        redisService.updateEmployee(id, prev_employee);
        return repo.save(prev_employee);

    }

    public Employee updateEmployeeUsingQueue(Employee employee){
        try {
            System.out.println("[x] Requesting: " + employee);
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(employee);
            String updatedEmployee = (String) rabbitTemplate.convertSendAndReceive(RabbitmqConfig.Exchange, RabbitmqConfig.RoutingKey, json);
            if(updatedEmployee == null) return null;
            Employee updatedEmployeeObject = mapper.readValue(updatedEmployee, Employee.class);
            System.out.println("updated emp: " + updatedEmployeeObject);
            redisService.updateEmployee(employee.getId(), updatedEmployeeObject);
            return updatedEmployeeObject;
        } catch (Exception e) {
            e.printStackTrace();
            employee.setId("false");
            return employee;
        }

    }

    public Boolean deleteEmp(String id){
        Employee prev_employee = redisService.getEmployee(id);
        if(prev_employee == null){
            System.out.println("getting emp: " + id + " from db, cache don't have it");
            prev_employee = repo.findById(id).get();
            redisService.setEmployee(id, prev_employee);
        } else {
            System.out.println("got emp: " + id + " from cache");
        }
        if(prev_employee != null){
            if(prev_employee.isDeleted() == true){
                return false;
            }
            prev_employee.setDeleted(true);
            redisService.deleteKey(id);
            repo.save(prev_employee);
            return true;
        }

        return false;
    }
}