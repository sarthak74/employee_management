package com.example.employee.exceptionController;

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

    // @ExceptionHandler(TransactionSystemException.class)
    // public ResponseEntity<String> handleIncorrectConstraints(TransactionSystemException transactionSystemException) {
    //     System.out.println("trans exc");
    //     System.out.println(transactionSystemException.getMostSpecificCause());
        
    //     // System.out.println(transactionSystemException.getLocalizedMessage());
    //     // System.out.println(transactionSystemException.getOriginalException());
        
    //     return new ResponseEntity<String>("Incorrect values for some fields", HttpStatus.BAD_REQUEST);
    // }

}
