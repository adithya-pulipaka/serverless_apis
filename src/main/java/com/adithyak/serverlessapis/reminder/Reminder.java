package com.adithyak.serverlessapis.reminder;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "reminders")
public class Reminder {

    @Id
    private String id;
    private String title;
    private String description;
    private Instant remindAt;
    private RecurPattern recurPattern = RecurPattern.NONE;
    private ReminderStatus status = ReminderStatus.ACTIVE;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
