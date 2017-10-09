package cz.cuni.mff.fruiton.component;

import com.google.protobuf.InvalidProtocolBufferException;
import cz.cuni.mff.fruiton.dto.CommonProtos;
import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.service.authentication.RegistrationService;
import cz.cuni.mff.fruiton.test.util.TestUtils;
import cz.cuni.mff.fruiton.test.util.TestWebSocketClient;
import cz.cuni.mff.fruiton.util.KernelUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URISyntaxException;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = cz.cuni.mff.fruiton.Application.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class GameTest {

    private static final String LOGIN1 = "test1";
    private static final String LOGIN2 = "test2";

    private static final String PASSWORD = "password";

    @Autowired
    private RegistrationService registrationService;

    private TestWebSocketClient client1;
    private TestWebSocketClient client2;

    @Before
    public void setup() throws URISyntaxException, InterruptedException {

        registrationService.register(TestUtils.getRegistrationData("test1@test.com", LOGIN1, PASSWORD));
        registrationService.register(TestUtils.getRegistrationData("test2@test.com", LOGIN2, PASSWORD));

        String token1 = TestUtils.login(LOGIN1, PASSWORD);
        String token2 = TestUtils.login(LOGIN2, PASSWORD);

        client1 = new TestWebSocketClient(token1);
        client2 = new TestWebSocketClient(token2);

        client1.connectBlocking();
        client2.connectBlocking();
    }

    @Test
    public void matchMakingTest() throws InvalidProtocolBufferException, InterruptedException {

        client1.send(buildFindGameMsg("team1", List.of(1), List.of(KernelUtils.positionOf(0, 0))).toByteArray());
        client2.send(buildFindGameMsg("team2", List.of(1), List.of(KernelUtils.positionOf(0, 0))).toByteArray());

        CommonProtos.WrapperMessage message1 = client1.blockingPoll();
        CommonProtos.WrapperMessage message2 = client2.blockingPoll();

        assertEquals("After successful matchmaking GameReady message is expected from server",
                CommonProtos.WrapperMessage.MessageCase.GAMEREADY, message1.getMessageCase());
        assertEquals("After successful matchmaking GameReady message is expected from server",
                CommonProtos.WrapperMessage.MessageCase.GAMEREADY, message2.getMessageCase());
    }

    private CommonProtos.WrapperMessage buildFindGameMsg(
            final String teamName,
            final Iterable<Integer> fruitonIds,
            final Iterable<GameProtos.Position> fruitonPositions
    ) {
        return CommonProtos.WrapperMessage.newBuilder()
                .setFindGame(GameProtos.FindGame.newBuilder()
                        .setTeam(GameProtos.FruitonTeam.newBuilder()
                                .setName(teamName)
                                .addAllFruitonIDs(fruitonIds)
                                .addAllPositions(fruitonPositions)
                                .build())
                        .build())
                .build();
    }

}