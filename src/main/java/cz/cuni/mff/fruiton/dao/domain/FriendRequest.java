package cz.cuni.mff.fruiton.dao.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public final class FriendRequest {

    @Id
    private String id;

    @DBRef
    private User from;

    @DBRef
    private User to;

    public FriendRequest() {
    }

    public FriendRequest(final User from, final User to) {
        this.from = from;
        this.to = to;
    }

    public User getFrom() {
        return from;
    }

    public void setFrom(final User from) {
        this.from = from;
    }

    public User getTo() {
        return to;
    }

    public void setTo(final User to) {
        this.to = to;
    }
}
