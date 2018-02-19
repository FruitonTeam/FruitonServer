package cz.cuni.mff.fruiton.service.social;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dao.domain.Achievement;
import cz.cuni.mff.fruiton.dao.domain.FruitonTeam;
import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.dto.form.EditProfileForm;
import fruiton.kernel.Fruiton;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface UserService {

    class PlayerInfo {

        private final String login;
        private final int rating;
        private final String avatar;
        private final GameProtos.Fraction fraction;

        public PlayerInfo(final String login, final int rating, final String avatar, final GameProtos.Fraction fraction) {
            this.login = login;
            this.rating = rating;
            this.avatar = avatar;
            this.fraction = fraction;
        }

        public String getLogin() {
            return login;
        }

        public int getRating() {
            return rating;
        }

        public String getAvatar() {
            return avatar;
        }

        public GameProtos.Fraction getFraction() {
            return fraction;
        }
    }

    void changeAvatar(UserIdHolder user, MultipartFile avatar);

    void changeAvatar(UserIdHolder user, String url);

    Optional<String> getBase64Avatar(UserIdHolder user);

    Optional<String> getBase64Avatar(String login);

    void changePassword(UserIdHolder user, String newPassword);

    void changeEmail(UserIdHolder user, String newEmail);

    UserIdHolder findUserByLogin(String login);

    UserIdHolder tryFindUserByLogin(String login);

    UserIdHolder findUser(String id);

    GameProtos.LoggedPlayerInfo getLoggedPlayerInfo(UserIdHolder user);

    String generateRandomName(GoogleIdToken.Payload payload);

    void removeFruitonFromUnlockedFruitons(UserIdHolder user, int fruitonId);

    void unlockFruiton(UserIdHolder user, int fruitonId);

    void adjustMoney(UserIdHolder user, int change);

    void addFruitonTeam(UserIdHolder idHolder, FruitonTeam teamToAdd);

    void removeTeam(UserIdHolder idHolder, String teamToRemove);

    List<Integer> getAvailableFruitons(UserIdHolder idHolder);

    List<Fruiton> getFruitonsAvailableForSelling(UserIdHolder idHolder);

    List<FruitonTeam> getFruitonTeams(UserIdHolder idHolder);

    void unlockAchievement(UserIdHolder idHolder, Achievement achievement);

    List<Achievement> getUnlockedAchievements(UserIdHolder idHolder);

    PlayerInfo getPlayerInfo(String login);

    GameProtos.PlayerInfo getProtobufPlayerInfo(UserIdHolder player);

    int getRating(UserIdHolder idHolder);

    boolean isAvatarSet(UserIdHolder idHolder);

    EditProfileForm getEditProfileForm(UserIdHolder idHolder);

    List<UserIdHolder> getFriends(UserIdHolder idHolder);

    void setFraction(UserIdHolder user, GameProtos.Fraction fraction);

    boolean teamContainsUnlockedFruitons(UserIdHolder user, FruitonTeam team);

    void removeFriend(UserIdHolder user, UserIdHolder friendToRemove);

    void setRating(UserIdHolder user, int newRating);

}
