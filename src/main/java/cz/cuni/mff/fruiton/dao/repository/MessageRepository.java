package cz.cuni.mff.fruiton.dao.repository;

import cz.cuni.mff.fruiton.dao.domain.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRepository extends MongoRepository<Message, String> {

}
