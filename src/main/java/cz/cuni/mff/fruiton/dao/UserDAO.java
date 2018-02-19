package cz.cuni.mff.fruiton.dao;

import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public final class UserDAO {

    private final UserRepository userRepository;

    @Autowired
    public UserDAO(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserIdHolder> getFriends(final UserIdHolder user) {
        return userRepository.findOne(user.getId()).getFriends().stream()
                .map(UserIdHolder::of)
                .collect(Collectors.toList());
    }

}
