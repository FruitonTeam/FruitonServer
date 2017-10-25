package cz.cuni.mff.fruiton.dao.repository;

import cz.cuni.mff.fruiton.dao.domain.MailConfirmation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EmailConfirmationRepository extends MongoRepository<MailConfirmation, String> {

}
