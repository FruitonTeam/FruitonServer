package cz.cuni.mff.fruiton.dao.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class MailConfirmation {

    @Id
    private String id;

    @DBRef
    private User user;

    public final String getId() {
        return id;
    }

    public final void setId(final String id) {
        this.id = id;
    }

    public final User getUser() {
        return user;
    }

    public final void setUser(final User user) {
        this.user = user;
    }

}
