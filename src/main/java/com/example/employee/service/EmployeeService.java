package com.example.employee.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.example.employee.entity.Employee;
import com.example.employee.repository.EmployeeRepository;

@Service
@EnableCaching
public class EmployeeService {

    @Autowired
    private EmployeeRepository repo;

    private String REDIS_HASH = "REDIS_KEY";

    public Employee save(Employee employee){
        System.out.println("emp service: " + employee);

        return repo.save(employee);
    }

    public List<Employee> saveAll(List<Employee> employees){
        return repo.saveAll(employees);
    }


    @Cacheable(value = "employee")
    public List<Employee> getAll(){
        System.out.println("get all emps");
        return repo.findAllEmployees();
    }

    @Cacheable(value = "employee", key = "#id", condition = "#result != null")
    public Employee getEmp(String id){
        System.out.println("get emp by id: " + id);
        Employee employee = repo.findById(id).get();
        if(employee == null) return null;
        if(employee.isDeleted()) return null;
        return employee;
    }

    @CachePut(value = "employee", key = "#employee.getId()")
    public Employee updateEmployee(Employee employee){
        System.out.println("updating emp id: " + employee.getId());
        Employee prev_employee = repo.findById(employee.getId()).get();

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

        return repo.save(prev_employee);

    }

    @CacheEvict(value = "employee", key = "#id")
    public Boolean deleteEmp(String id){
        Employee prev_employee = repo.findById(id).get();
        if(prev_employee != null){
            if(prev_employee.isDeleted() == true){
                return false;
            }
            prev_employee.setDeleted(true);
            repo.save(prev_employee);
            return true;
        }
        
        return false;
    }
}
