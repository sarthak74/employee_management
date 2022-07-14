package com.example.employee.service;

import com.example.employee.entity.Employee;
import com.google.gson.Gson;
import org.omg.CORBA.TIMEOUT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmployeeRedisService {
    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate template;

    private String allEmployeesKey = "allEmployeesKey";

    private String redisHash = "employee";

    @Autowired
    Gson gson;

    public void setEmployee(String key, Employee employee){
        template.opsForValue().set(key, employee);
        Map<String, Employee> allEmployees = (Map<String, Employee>) template.opsForValue().get(allEmployeesKey);
        allEmployees.put(employee.getId(), employee);
        template.opsForValue().set(allEmployeesKey, allEmployees);
    }

    public Employee getEmployee(String key){
        Employee employee = (Employee) template.opsForValue().get(key);
        return employee;
    }

    public void setAllEmployees(List<Employee> employees){
        Map<String, Employee> allEmployees = (Map<String, Employee>) template.opsForValue().get(allEmployeesKey);
        if (allEmployees == null) {
            allEmployees = new HashMap<String, Employee>();
            
        }
        Map<String, Employee> finalAllEmployees = employees.stream().collect(Collectors.toMap(employee -> employee.getId(), employee -> employee));
        allEmployees = finalAllEmployees;
        template.opsForValue().set(allEmployeesKey, allEmployees);
    }

    public Map<String, Employee> getAllEmployees(){
        Map<String, Employee> employees = (Map<String, Employee>) template.opsForValue().get(allEmployeesKey);
        return employees;
    }

    public void updateEmployee(String key, Employee newEmployee) {
        template.opsForValue().set(key, newEmployee);
        Map<String, Employee> allEmployees = (Map<String, Employee>) template.opsForValue().get(allEmployeesKey);
        allEmployees.replace(newEmployee.getId(), newEmployee);
        template.opsForValue().set(allEmployeesKey, allEmployees);
    }

    public void deleteKey(String id){
        template.delete(id);
        Map<String, Employee> allEmployees = (Map<String, Employee>) template.opsForValue().get(allEmployeesKey);
        allEmployees.remove(id);
        template.opsForValue().set(allEmployeesKey, allEmployees);
    }
}
