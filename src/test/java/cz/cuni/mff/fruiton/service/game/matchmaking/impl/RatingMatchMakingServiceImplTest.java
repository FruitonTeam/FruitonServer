package cz.cuni.mff.fruiton.service.game.matchmaking.impl;

import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.service.game.GameService;
import cz.cuni.mff.fruiton.test.util.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

    @InjectMocks
    private RatingMatchMakingServiceImpl ratingMatchMakingService;

    @Captor
    private ArgumentCaptor<User> userCaptor1;

    @Captor
    private ArgumentCaptor<User> userCaptor2;

    @Test
    public void testMatchMakingService() {
        User user1 = new User().withLogin("login1");
        user1.setRating(1000);
        User user2 = new User().withLogin("login2");
        user2.setRating(1000);

        User user3 = new User().withLogin("login3");
        user3.setRating(400);
        User user4 = new User().withLogin("login4");
        user4.setRating(400);

        synchronized (ratingMatchMakingService) {
            ratingMatchMakingService.findGame(user1, TestUtils.buildFindGameMsg());
            ratingMatchMakingService.findGame(user2, TestUtils.buildFindGameMsg());

            ratingMatchMakingService.findGame(user3, TestUtils.buildFindGameMsg());
            ratingMatchMakingService.findGame(user4, TestUtils.buildFindGameMsg());

            ratingMatchMakingService.match();
        }

        verify(gameService, times(2))
                .createGame(userCaptor1.capture(), any(GameProtos.FruitonTeam.class), userCaptor2.capture(),
                        any(GameProtos.FruitonTeam.class));

        assertMatchedAgainstEachOther(user1, user2, userCaptor1.getAllValues(), userCaptor2.getAllValues());
    }

    private void assertMatchedAgainstEachOther(User user1, User user2, List<User> matched1, List<User> matched2) {
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
        User user1 = new User().withLogin("login1");
        user1.setRating(100);
        User user2 = new User().withLogin("login2");
        user2.setRating(1500);

        User user3 = new User().withLogin("login3");
        user3.setRating(2000);

        synchronized (ratingMatchMakingService) {
            ratingMatchMakingService.findGame(user1, TestUtils.buildFindGameMsg());
            ratingMatchMakingService.findGame(user2, TestUtils.buildFindGameMsg());

            ratingMatchMakingService.findGame(user3, TestUtils.buildFindGameMsg());

            ratingMatchMakingService.match();

            verify(gameService, never()).createGame(any(User.class), any(GameProtos.FruitonTeam.class),
                    any(User.class), any(GameProtos.FruitonTeam.class));

            for (int i = 0; i < 50; i++) {
                ratingMatchMakingService.match();
            }
        }

        verify(gameService, times(1))
                .createGame(userCaptor1.capture(), any(GameProtos.FruitonTeam.class), userCaptor2.capture(),
                        any(GameProtos.FruitonTeam.class));

        assertMatchedAgainstEachOther(user2, user3, userCaptor1.getAllValues(), userCaptor2.getAllValues());
    }

}