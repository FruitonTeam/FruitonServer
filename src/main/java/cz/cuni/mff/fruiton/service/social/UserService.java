package cz.cuni.mff.fruiton.service.social;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dao.domain.Achievement;
import cz.cuni.mff.fruiton.dao.domain.FruitonTeam;
import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.dto.GameProtos.Fraction;
import cz.cuni.mff.fruiton.dto.GameProtos.LoggedPlayerInfo;
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
        private final Fraction fraction;

        public PlayerInfo(final String login, final int rating, final String avatar, final Fraction fraction) {
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

        public Fraction getFraction() {
            return fraction;
        }
    }

    /**
     * Changes avatar for specified user.
     * @param user user for whom to change the avatar
     * @param avatar new avatar image
     */
    void changeAvatar(UserIdHolder user, MultipartFile avatar);

    /**
     * Changes avatar for specified user.
     * @param user user for whom to change the avatar
     * @param url url of the new avatar image
     */
    void changeAvatar(UserIdHolder user, String url);

    /**
     * Returns base64 representation of the provided user avatar.
     * @param user user for whom to fetch the avatar
     * @return base64 representation of the provided user avatar
     */
    Optional<String> getBase64Avatar(UserIdHolder user);

    /**
     * Returns base64 representation of the provided user avatar.
     * @param login login of the user for whom to fetch the avatar
     * @return base64 representation of the provided user avatar
     */
    Optional<String> getBase64Avatar(String login);

    /**
     * Changes password for specified user.
     * @param user user for whom to change the password
     * @param newPassword new password value
     */
    void changePassword(UserIdHolder user, String newPassword);

    /**
     * Changes email for specified user.
     * @param user user for whom to change the email
     * @param newEmail new email value
     */
    void changeEmail(UserIdHolder user, String newEmail);

    /**
     * Finds user by login.
     * @param login login of the user
     * @return user with provided login
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException if no user with provided login exists
     */
    UserIdHolder findUserByLogin(String login);

    /**
     * Tries to find user with provided login.
     * @param login login of the user
     * @return user with provided login or null if no such user exists
     */
    UserIdHolder tryFindUserByLogin(String login);

    /**
     * Finds user by his id.
     * @param id id of the user
     * @return user with provided id
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException if no such user exists
     */
    UserIdHolder findUser(String id);

    /**
     * Returns information needed for newly logged in user.
     * @param user user for whom to get the information
     * @return information needed for newly logged in user
     */
    LoggedPlayerInfo getLoggedPlayerInfo(UserIdHolder user);

    /**
     * Generates random name for given information about Google account.
     * @param payload Google account basic information
     * @return random name
     */
    String generateRandomName(GoogleIdToken.Payload payload);

    /**
     * Removes specified fruiton from unlocked fruitons.
     * @param user user for whom to remove the fruiton
     * @param fruitonId fruiton to remove
     */
    void removeFruitonFromUnlockedFruitons(UserIdHolder user, int fruitonId);

    /**
     * Unlocks fruiton for specified user.
     * @param user user for whom to unlock the fruiton
     * @param fruitonId fruiton to unlock
     */
    void unlockFruiton(UserIdHolder user, int fruitonId);

    /**
     * Adjusts money for specified user.
     * @param user user for whom to adjust money
     * @param amount value by which to change the money's amount
     */
    void adjustMoney(UserIdHolder user, int amount);

    /**
     * Adds team to user's teams.
     * @param user user for whom to add the team
     * @param teamToAdd team to add
     */
    void addFruitonTeam(UserIdHolder user, FruitonTeam teamToAdd);

    /**
     * Removes team from user's teams.
     * @param user user for whom to remove the team
     * @param teamToRemove name of the team to remove
     */
    void removeTeam(UserIdHolder user, String teamToRemove);

    /**
     * Returns all fruitons which provided user can use.
     * @param user user for whom to get the fruitons
     * @return ids of the all fruitons which provided user can use
     */
    List<Integer> getAvailableFruitons(UserIdHolder user);

    /**
     * Returns all fruitons which provided user can sell.
     * @param user
     * @return
     */
    List<Fruiton> getFruitonsAvailableForSelling(UserIdHolder user);

    /**
     * Returns all teams of specified user.
     * @param user user whose teams to fetch
     * @return all teams of specified user
     */
    List<FruitonTeam> getFruitonTeams(UserIdHolder user);

    /**
     * Unlocks specified achievement.
     * @param user user for whom the achievement will be unlocked
     * @param achievement achievement to unlock
     */
    void unlockAchievement(UserIdHolder user, Achievement achievement);

    /**
     * Returns all unlocked achievements for specified user.
     * @param user user for whom to fetch unlocked achievements
     * @return all user's unlocked achievements
     */
    List<Achievement> getUnlockedAchievements(UserIdHolder user);

    /**
     * Returns basic player info for user with given login.
     * @param login login of the user
     * @return basic player info
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException if no user with given login exists
     */
    PlayerInfo getPlayerInfo(String login);

    /**
     * Returns basic player info for user with given login.
     * @param player user for whom to fetch the info
     * @return basic player info
     */
    GameProtos.PlayerInfo getProtobufPlayerInfo(UserIdHolder player);

    /**
     * Returns rating for specified user.
     * @param user user for whom to fetch the rating
     * @return rating of the user
     */
    int getRating(UserIdHolder user);

    /**
     * Determines whether user has custom avatar set.
     * @param user user for whom to determine if he has an avatar set
     * @return true if user has custom avatar set, false otherwise
     */
    boolean isAvatarSet(UserIdHolder user);

    /**
     * Returns filled edit profile form for provided user.
     * @param user user for whom to get edit profile form
     * @return edit profile form for provided user
     */
    EditProfileForm getEditProfileForm(UserIdHolder user);

    /**
     * Returns all friends of specified user.
     * @param user user for whom to fetch all friends
     * @return all friends of specified user
     */
    List<UserIdHolder> getFriends(UserIdHolder user);

    /**
     * Sets fraction for specified user.
     * @param user user for whom to set the fraction
     * @param fraction fraction to set
     */
    void setFraction(UserIdHolder user, Fraction fraction);

    /**
     * Determines whether team contains fruitons for which the provided user can play.
     * @param user user for whom to perform the check
     * @param team team to check
     * @return true if team contains fruitons available to {@code user}, false otherwise
     */
    boolean teamContainsAvailableFruitons(UserIdHolder user, FruitonTeam team);

    /**
     * Removes friend for given user.
     * @param user user for whom to remove friend
     * @param friendToRemove friend to remove
     */
    void removeFriend(UserIdHolder user, UserIdHolder friendToRemove);

    /**
     * Sets new rating for specified user.
     * @param user user for whom to set the rating
     * @param newRating new rating to be set
     */
    void setRating(UserIdHolder user, int newRating);

}
