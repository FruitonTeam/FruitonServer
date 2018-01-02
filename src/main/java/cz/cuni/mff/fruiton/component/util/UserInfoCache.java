package cz.cuni.mff.fruiton.component.util;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

@Component
public final class UserInfoCache {

    private static UserInfoCache instance;

    private UserRepository userRepository;

    private static Map<UserIdHolder, UserInfo> cache = Collections.synchronizedMap(new WeakHashMap<>());

    @Autowired
    private UserInfoCache(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    private void init() {
        instance = this;
    }

    public static UserInfo get(final UserIdHolder idHolder) {
        if (idHolder == null) {
            throw new IllegalArgumentException("Cannot get info for null user");
        }

        UserInfo info = cache.get(idHolder);
        if (info != null) {
            return info;
        }

        User user = instance.userRepository.findOne(idHolder.getId());
        UserInfo newUserinfo = new UserInfo(user.getAvatarWebImageMapping(), user.getMoney());
        cache.put(idHolder, newUserinfo);

        return newUserinfo;
    }

    public static void invalidate(final UserIdHolder idHolder) {
        if (idHolder == null) {
            throw new IllegalArgumentException("Cannot invalidate null user from cache");
        }
        cache.remove(idHolder);
    }

    public static class UserInfo {

        private String avatar;
        private int money;

        private UserInfo(final String avatar, final int money) {
            this.avatar = avatar;
            this.money = money;
        }

        public String getAvatar() {
            return avatar;
        }

        public int getMoney() {
            return money;
        }
    }

}
