package com.example.employee.repository;

import com.example.employee.entity.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmployeeRedisRepo {

    private static final Logger log = LoggerFactory.getLogger(EmployeeRedisRepo.class);

    @Autowired
    private EmployeeRepository repo;

    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate template;

    private String allEmployeesKey = "allEmployeesKey";

    private String redisHash = "employee";

    public void setEmployee(String key, Employee employee){
        template.opsForValue().set(key, employee);
    }

    public Employee getEmployee(String key){
        Employee employee = (Employee) template.opsForValue().get(key);
        return employee;
    }

    public void setAllEmployees(List<Employee> employees){
        Map<String, Employee> allEmployees = getAllEmployees();
        Map<String, Employee> finalAllEmployees = allEmployees;
        employees.stream().forEach(employee -> finalAllEmployees.put(employee.getId(), employee));
        allEmployees = finalAllEmployees;
        template.opsForValue().set(allEmployeesKey, allEmployees);
    }

    public Map<String, Employee> getAllEmployees(){
        Map<String, Employee> allEmployees = (Map<String, Employee>) template.opsForValue().get(allEmployeesKey);
        if (allEmployees == null) {
            List<Employee> allSavedEmployeesList = repo.findAllEmployees();
            Map<String, Employee> allSavedEmployeesMap = new HashMap<String, Employee>();
            allSavedEmployeesList.stream().forEach(employee -> allSavedEmployeesMap.put(employee.getId(), employee));
            allEmployees = allSavedEmployeesMap;
        }
        return allEmployees;
    }

    public void setAnEmployeeToCache(String key, Employee employee){
        setEmployee(key, employee);
        List<Employee> newEmployeesList = new ArrayList<Employee>();
        newEmployeesList.add(employee);
        setAllEmployees(newEmployeesList);
    }

    public void updateEmployee(String key, Employee newEmployee) {
        setEmployee(key, newEmployee);
        List<Employee> newEmployeesList = new ArrayList<Employee>();
        newEmployeesList.add(newEmployee);
        setAllEmployees(newEmployeesList);
    }

    public void deleteKey(String id){
        template.delete(id);
        Map<String, Employee> allEmployees = getAllEmployees();
        allEmployees.remove(id);
        template.opsForValue().set(allEmployeesKey, allEmployees);
    }


}
