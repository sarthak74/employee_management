package com.example.employee.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
public class Employee implements Serializable {
    
    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private int id;
    //uuid

    @NotNull
    private String name;

    @NotNull
    private String pod;

    @NotNull
    private String contact;

    @NotNull
    @Max(value=60, message = "Age can't be greater than 60")
    @Min(value=15, message = "Age can't be less than 15")
    private Integer age;

    @JsonFormat(pattern = "dd-MM-YYYY HH:mm:ss")
    @CreationTimestamp
    @Column(name = "createdAt", updatable = false)
    private Timestamp createdAt;

    @JsonFormat(pattern = "dd-MM-YYYY HH:mm:ss")
    @UpdateTimestamp
    @Column(name = "updateAt")
    private Timestamp updatedAt;

    private boolean isDeleted = false;
    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getPod(){
        return pod;
    }

    public String getContact(){
        return contact;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPod(String pod) {
        this.pod = pod;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    @Override
    public String toString() {
        return "Employee [age=" + age + ", contact=" + contact + ", createdAt=" + createdAt + ", id=" + id
                + ", isDeleted=" + isDeleted + ", name=" + name + ", pod=" + pod + ", updatedAt=" + updatedAt + "]";
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    
}
