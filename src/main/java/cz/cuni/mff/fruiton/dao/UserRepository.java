package cz.cuni.mff.fruiton.dao;

import cz.cuni.mff.fruiton.dao.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {

    User findByLogin(String login);

}
