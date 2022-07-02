package com.example.employee.exceptionController;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.stream.StreamSupport;

import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


import java.util.*;

@ControllerAdvice
public class EmployeeExceptionController {
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> noElementHandler(NoSuchElementException noSuchElementException){
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

    @ExceptionHandler(JpaSystemException.class)
    public ResponseEntity<String> handleJPAExceptions(JpaSystemException jpaSystemException){
        
        return new ResponseEntity<String>("Bad data format", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<String> handleTransactionSystemExceptions(TransactionSystemException transactionSystemException) {
        System.out.println("tran sys exc: " + transactionSystemException);
        System.out.println("tran sys exc getm: " + transactionSystemException.getMessage());
        System.out.println("tran sys exc getLm: " + transactionSystemException.getLocalizedMessage());
        System.out.println("tran sys exc getC: " + transactionSystemException.getCause());
        return new ResponseEntity<String>("Incorrect data format", HttpStatus.BAD_REQUEST);
    }

}
