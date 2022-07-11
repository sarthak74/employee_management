package com.example.employee.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.example.employee.entity.Employee;
import com.example.employee.repository.EmployeeRepository;

@Repository
@EnableCaching
public class EmployeeService {

    @Qualifier("redisTemplate")
    @Autowired
    private RedisTemplate template;
    private static final String hash = "employee";
    private static final String REDIS_HASH = "employee";

    public Employee save(Employee employee){

        String id = UUID.randomUUID().toString();
        System.out.println("emp service: " + employee + "\nid: "+id);
        employee.setId(id);
        template.opsForHash().put(hash, employee.getId(), employee);
        return employee;
    }

//    public List<Employee> saveAll(List<Employee> employees){
//        return repo.saveAll(employees);
//    }


    public List<Employee> getAll(){
        return template.opsForHash().values(hash);
    }

    @Cacheable(value = "employee", key = "#id", condition = "#result != null")
    public Employee getEmp(String id){
        System.out.println("get emp by id: " + id);
        Employee employee = (Employee) template.opsForHash().get(hash, id);
        if(employee == null) return null;
        if(employee.isDeleted()) return null;
        return employee;
    }

    @CachePut(value = "employee", key = "#employee.getId()")
    public Employee updateEmployee(Employee employee){
        System.out.println("updating emp id: " + employee.getId());
        Employee prev_employee = (Employee) template.opsForHash().get(hash, employee.getId());

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
        template.opsForHash().put(hash, employee.getId(), prev_employee);
        return prev_employee;

    }

    @CacheEvict(value = "employee", key = "#id")
    public Boolean deleteEmp(String id){
        Employee prev_employee = (Employee) template.opsForHash().get(hash, id);
        if(prev_employee != null){
            if(prev_employee.isDeleted() == true){
                return false;
            }
            prev_employee.setDeleted(true);
            template.opsForHash().put(hash, id, prev_employee);
            return true;
        }
        
        return false;
    }
}
