package cz.cuni.mff.fruiton.dao.repository;

import cz.cuni.mff.fruiton.dao.domain.FriendRequest;
import cz.cuni.mff.fruiton.dao.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FriendRequestRepository extends MongoRepository<FriendRequest, String> {

    FriendRequest findByFromAndTo(User from, User to);

    boolean existsByFromAndTo(User from, User to);

    List<FriendRequest> findByTo(User to);

}
