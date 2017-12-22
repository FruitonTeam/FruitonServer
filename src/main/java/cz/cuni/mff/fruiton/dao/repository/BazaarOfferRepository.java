package cz.cuni.mff.fruiton.dao.repository;

import cz.cuni.mff.fruiton.dao.domain.BazaarOffer;
import cz.cuni.mff.fruiton.dao.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BazaarOfferRepository extends MongoRepository<BazaarOffer, String> {

    List<BazaarOffer> findByFruitonIdOrderByPriceAsc(int fruitonId);

    List<BazaarOffer> findByOfferedBy(User offeredBy);

}
