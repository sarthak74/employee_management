package com.example.employee.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

@Entity
@Data
@NoArgsConstructor
public class Employee implements Serializable {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name="id", length=36, updatable=false)
    private String id;
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

    // @JsonFormat(pattern = "dd-MM-YYYY HH:mm:ss")
    @CreationTimestamp
    @Column(name = "createdAt", updatable = false)
    private Timestamp createdAt;

    // @JsonFormat(pattern = "dd-MM-YYYY HH:mm:ss")
    @UpdateTimestamp
    @Column(name = "updateAt")
    private Timestamp updatedAt;

    private boolean isDeleted = false;

    @Override
    public String toString() {
        return "Employee [age=" + age + ", contact=" + contact + ", createdAt=" + createdAt + ", id=" + id
                + ", name=" + name + ", pod=" + pod + ", updatedAt=" + updatedAt + "]";
    }

    
    
}
