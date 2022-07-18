package com.example.employee.exceptionController;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.stream.StreamSupport;

import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


import java.util.*;

@ControllerAdvice
public class EmployeeExceptionController {
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> noElementHandler(NoSuchElementException noSuchElementException){
        String msg = noSuchElementException.getMessage();
        System.out.println("no such element found msg: " + msg);
        return new ResponseEntity<String>("No Employee exists with given data", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleBadInput(ConstraintViolationException constraintViolationException){
        
        List<String> fields = new ArrayList<String>();
        constraintViolationException.getConstraintViolations().stream().forEach(vio -> {
            fields.add((StreamSupport.stream(vio.getPropertyPath().spliterator(), false).reduce((first, second) -> second).orElse(null).toString())); 
        });
        return new ResponseEntity<String>("Incorrect Input fields: " + fields, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConnectException.class)
    public ResponseEntity<String> handleConnectionException(ConnectException connectException){
        System.out.println("Exc: " + connectException.getStackTrace());
        return new ResponseEntity<String>("Can't connect to servers correctly", HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
