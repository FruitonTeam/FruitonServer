package cz.cuni.mff.fruiton.dao.domain;

import cz.cuni.mff.fruiton.chat.MessageStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
public class Message {

    @Id
    private String id;

    @DBRef
    private User sender;

    @DBRef
    private User recipient;

    private String content;

    @CreatedDate
    private LocalDateTime created;

    private MessageStatus status = MessageStatus.NOT_DELIVERED;

    public final String getId() {
        return id;
    }

    public final void setId(final String id) {
        this.id = id;
    }

    public final User getSender() {
        return sender;
    }

    public final void setSender(final User sender) {
        this.sender = sender;
    }

    public final User getRecipient() {
        return recipient;
    }

    public final void setRecipient(final User recipient) {
        this.recipient = recipient;
    }

    public final String getContent() {
        return content;
    }

    public final void setContent(final String content) {
        this.content = content;
    }

    public final LocalDateTime getCreated() {
        return created;
    }

    public final void setCreated(final LocalDateTime created) {
        this.created = created;
    }

    public final MessageStatus getStatus() {
        return status;
    }

    public final void setStatus(final MessageStatus status) {
        this.status = status;
    }

}
