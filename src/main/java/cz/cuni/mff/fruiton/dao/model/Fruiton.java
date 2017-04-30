package cz.cuni.mff.fruiton.dao.model;

import cz.cuni.mff.fruiton.dto.GameProtos;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Fruiton {

    @Id
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GameProtos.Fruiton convertToDTO() {
        return GameProtos.Fruiton.newBuilder().setId(id).build();
    }
}
