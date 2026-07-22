package com.adithyak.serverlessapis.todo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TodoRepository extends MongoRepository<Todo, String> {
    List<Todo> findByStatus(TodoStatus status);
    List<Todo> findByPriority(TodoPriority priority);
    List<Todo> findByStatusAndPriority(TodoStatus status, TodoPriority priority);
}
