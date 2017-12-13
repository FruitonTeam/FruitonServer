package cz.cuni.mff.fruiton.dao.repository;

import cz.cuni.mff.fruiton.dao.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {

    User findByLogin(String login);

    boolean existsByLogin(String login);

    User findByEmail(String email);

    boolean existsByEmail(String email);

    User findByGoogleSubject(String googleSubject);

    Page<User> findAllByOrderByRatingDesc(Pageable pageable);

    boolean existsByLogin(String login);

}
