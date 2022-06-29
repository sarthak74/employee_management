package com.example.employee.service;

import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.employee.config.RabbitmqConfig;
import com.example.employee.entity.Employee;
import com.example.employee.repository.EmployeeRepository;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository repo;

    public Employee save(Employee employee){
        System.out.println("emp service: " + employee);
        return repo.save(employee);
    }

    public List<Employee> saveAll(List<Employee> employees){
        
        System.out.println("empall service: " + employees);
        return repo.saveAll(employees);
    }

    public List<Employee> getAll(){
        return repo.findAllEmployees();
    }

    public Employee getEmp(int id){
        Employee employee = repo.findById(id).get();
        if(employee == null) return null;
        if(employee.isDeleted()) return null;
        return employee;
    }

    public Employee updateEmployee(Employee employee){
        System.out.println("updating employee" + employee);
        Employee prev_employee = repo.findById(employee.getId()).get();

        if(prev_employee == null){
            return null;
        }

        if(prev_employee.isDeleted() == true) {
            System.out.println("User already deleted");
            return null;
        }

        prev_employee.setName(employee.getName());
        prev_employee.setPod(employee.getPod());
        prev_employee.setContact(employee.getContact());
        prev_employee.setAge(employee.getAge());

        return repo.save(prev_employee);

    }

    public String deleteEmp(int id){
        Employee prev_employee = repo.findById(id).get();
        if(prev_employee != null){
            if(prev_employee.isDeleted() == true){
                System.out.println(("user already deleted"));
                return "No employee with id: " + id;
            }
            System.out.println("deleting emp: " + prev_employee);
            prev_employee.setDeleted(true);
            repo.save(prev_employee);
            return "Employee removed with id: " + id;
        }
        
        return "No employee with id: " + id;
    }

    @RabbitListener(queues = RabbitmqConfig.QueueName)
    public Employee updateEmployeeListener(Employee employee) {
        System.out.println("[.] Got request: " + employee);
        return updateEmployee(employee);
    }

}
