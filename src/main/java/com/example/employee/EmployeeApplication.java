package com.example.employee;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.employee.config.RabbitmqRPCClient;

@SpringBootApplication
public class EmployeeApplication {
	
    

	public static void main(String[] args) {
		
			SpringApplication.run(EmployeeApplication.class, args);	
		
	}


}

