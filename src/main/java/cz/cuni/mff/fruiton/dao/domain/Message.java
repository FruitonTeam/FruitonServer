package cz.cuni.mff.fruiton.dao.domain;

import cz.cuni.mff.fruiton.dto.ChatProtos;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Document
public final class Message {

    @Id
    private String id;

    @DBRef
    private User sender;

    @DBRef
    private User recipient;

    private String content;

    @CreatedDate
    private LocalDateTime created;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(final User sender) {
        this.sender = sender;
    }

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(final User recipient) {
        this.recipient = recipient;
    }

    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(final LocalDateTime created) {
        this.created = created;
    }

    public long getTimestamp() {
        return created.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    public ChatProtos.ChatMessage toProtobuf() {
        return ChatProtos.ChatMessage.newBuilder()
                .setId(id)
                .setRecipient(recipient.getLogin())
                .setSender(sender.getLogin())
                .setMessage(content)
                .setTimestamp(getTimestamp())
                .build();
    }

}
