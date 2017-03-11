package cz.cuni.mff.fruiton.dao;

import cz.cuni.mff.fruiton.dao.model.MailConfirmation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailConfirmationRepository extends MongoRepository<MailConfirmation, String> {

}
