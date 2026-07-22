package com.adithyak.serverlessapis.reminder;

import com.adithyak.serverlessapis.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReminderService {

    private final ReminderRepository repository;

    public ReminderService(ReminderRepository repository) {
        this.repository = repository;
    }

    public List<Reminder> findAll(ReminderStatus status) {
        return status != null ? repository.findByStatus(status) : repository.findAll();
    }

    public Reminder findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder", id));
    }

    public Reminder create(ReminderRequest request) {
        Reminder reminder = new Reminder();
        reminder.setTitle(request.title());
        reminder.setDescription(request.description());
        reminder.setRemindAt(request.remindAt());
        if (request.recurPattern() != null) reminder.setRecurPattern(request.recurPattern());
        return repository.save(reminder);
    }

    public Reminder update(String id, ReminderRequest request) {
        Reminder reminder = findById(id);
        reminder.setTitle(request.title());
        reminder.setDescription(request.description());
        reminder.setRemindAt(request.remindAt());
        if (request.recurPattern() != null) reminder.setRecurPattern(request.recurPattern());
        return repository.save(reminder);
    }

    /** Marks a reminder as DISMISSED, preventing future notifications. */
    public Reminder dismiss(String id) {
        Reminder reminder = findById(id);
        reminder.setStatus(ReminderStatus.DISMISSED);
        return repository.save(reminder);
    }

    public void delete(String id) {
        findById(id);
        repository.deleteById(id);
    }
}
