package com.adithyak.serverlessapis.todo;

import com.adithyak.serverlessapis.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TodoService {

    private final TodoRepository repository;

    public TodoService(TodoRepository repository) {
        this.repository = repository;
    }

    public List<Todo> findAll(TodoStatus status, TodoPriority priority) {
        if (status != null && priority != null) return repository.findByStatusAndPriority(status, priority);
        if (status != null) return repository.findByStatus(status);
        if (priority != null) return repository.findByPriority(priority);
        return repository.findAll();
    }

    public Todo findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo", id));
    }

    public Todo create(TodoRequest request) {
        Todo todo = new Todo();
        todo.setTitle(request.title());
        todo.setDescription(request.description());
        if (request.status() != null) todo.setStatus(request.status());
        if (request.priority() != null) todo.setPriority(request.priority());
        todo.setDueDate(request.dueDate());
        return repository.save(todo);
    }

    public Todo update(String id, TodoRequest request) {
        Todo todo = findById(id);
        todo.setTitle(request.title());
        todo.setDescription(request.description());
        if (request.status() != null) todo.setStatus(request.status());
        if (request.priority() != null) todo.setPriority(request.priority());
        todo.setDueDate(request.dueDate());
        return repository.save(todo);
    }

    public Todo updateStatus(String id, TodoStatus status) {
        Todo todo = findById(id);
        todo.setStatus(status);
        return repository.save(todo);
    }

    public void delete(String id) {
        findById(id);
        repository.deleteById(id);
    }
}
