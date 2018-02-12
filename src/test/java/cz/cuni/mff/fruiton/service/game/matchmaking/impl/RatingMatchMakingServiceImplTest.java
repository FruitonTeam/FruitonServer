package cz.cuni.mff.fruiton.service.game.matchmaking.impl;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.service.game.GameService;
import cz.cuni.mff.fruiton.service.game.matchmaking.TeamDraftService;
import cz.cuni.mff.fruiton.service.social.UserService;
import cz.cuni.mff.fruiton.service.util.UserStateService;
import cz.cuni.mff.fruiton.test.util.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = cz.cuni.mff.fruiton.Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("production")
public class RatingMatchMakingServiceImplTest {

    @Mock
    private GameService gameService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserStateService userStateService;

    @Autowired
    private TeamDraftService draftService;

    private RatingMatchMakingServiceImpl ratingMatchMakingService;

    @Captor
    private ArgumentCaptor<UserIdHolder> userCaptor1;

    @Captor
    private ArgumentCaptor<UserIdHolder> userCaptor2;

    @Before
    public void setup() {
        ratingMatchMakingService = new RatingMatchMakingServiceImpl(gameService, userService, userStateService, draftService);
    }

    @Test
    public void testMatchMakingService() {
        UserIdHolder user1 = TestUtils.createUser(userRepository, "login1", 1000);
        UserIdHolder user2 = TestUtils.createUser(userRepository, "login2", 1000);

        UserIdHolder user3 = TestUtils.createUser(userRepository, "login3", 400);
        UserIdHolder user4 = TestUtils.createUser(userRepository, "login4", 400);

        synchronized (ratingMatchMakingService) {
            ratingMatchMakingService.findGame(user1, TestUtils.buildFindGameMsg());
            ratingMatchMakingService.findGame(user2, TestUtils.buildFindGameMsg());

            ratingMatchMakingService.findGame(user3, TestUtils.buildFindGameMsg());
            ratingMatchMakingService.findGame(user4, TestUtils.buildFindGameMsg());

            ratingMatchMakingService.match();
        }

        verify(gameService, times(2))
                .createGame(
                        userCaptor1.capture(),
                        any(GameProtos.FruitonTeam.class),
                        userCaptor2.capture(),
                        any(GameProtos.FruitonTeam.class),
                        any(GameProtos.GameMode.class)
                );

        assertMatchedAgainstEachOther(user1, user2, userCaptor1.getAllValues(), userCaptor2.getAllValues());
    }

    private void assertMatchedAgainstEachOther(
            final UserIdHolder user1,
            final UserIdHolder user2,
            final List<UserIdHolder> matched1,
            final List<UserIdHolder> matched2
    ) {
        for (int i = 0; i < matched1.size(); i++) {
            if ((matched1.get(i).equals(user1) && matched2.get(i).equals(user2))
                    || matched1.get(i).equals(user2) && matched2.get(i).equals(user1)) {
                return;
            }
        }

        fail();
    }

    @Test
    public void testIncreasingDeltaRatingWindow() {
        UserIdHolder user1 = TestUtils.createUser(userRepository, "login1", 100);
        UserIdHolder user2 = TestUtils.createUser(userRepository, "login2", 1500);
        UserIdHolder user3 = TestUtils.createUser(userRepository, "login3", 2000);

        synchronized (ratingMatchMakingService) {
            ratingMatchMakingService.findGame(user1, TestUtils.buildFindGameMsg());
            ratingMatchMakingService.findGame(user2, TestUtils.buildFindGameMsg());

            ratingMatchMakingService.findGame(user3, TestUtils.buildFindGameMsg());

            ratingMatchMakingService.match();

            verify(gameService, never())
                    .createGame(
                            any(UserIdHolder.class),
                            any(GameProtos.FruitonTeam.class),
                            any(UserIdHolder.class),
                            any(GameProtos.FruitonTeam.class),
                            any(GameProtos.GameMode.class)
                    );

            for (int i = 0; i < 50; i++) {
                ratingMatchMakingService.match();
            }
        }

        verify(gameService, times(1))
                .createGame(
                        userCaptor1.capture(),
                        any(GameProtos.FruitonTeam.class),
                        userCaptor2.capture(),
                        any(GameProtos.FruitonTeam.class),
                        any(GameProtos.GameMode.class)
                );

        assertMatchedAgainstEachOther(user2, user3, userCaptor1.getAllValues(), userCaptor2.getAllValues());
    }

}