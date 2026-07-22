package com.adithyak.serverlessapis.todo;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Document(collection = "todos")
public class Todo {

    @Id
    private String id;
    private String title;
    private String description;
    private TodoStatus status = TodoStatus.PENDING;
    private TodoPriority priority = TodoPriority.MEDIUM;
    private LocalDate dueDate;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
