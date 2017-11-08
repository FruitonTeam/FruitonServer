package cz.cuni.mff.fruiton.component;

import com.google.protobuf.InvalidProtocolBufferException;
import cz.cuni.mff.fruiton.dto.CommonProtos;
import cz.cuni.mff.fruiton.service.authentication.RegistrationService;
import cz.cuni.mff.fruiton.test.util.TestUtils;
import cz.cuni.mff.fruiton.test.util.TestWebSocketClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = cz.cuni.mff.fruiton.Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("production")
public class GameTest {

    private static final String LOGIN1 = "test1";
    private static final String LOGIN2 = "test2";

    private static final String PASSWORD = "password";

    @Autowired
    private RegistrationService registrationService;

    @LocalServerPort
    private int port;

    private TestWebSocketClient client1;
    private TestWebSocketClient client2;

    @Before
    public void setup() throws URISyntaxException, InterruptedException {

        registrationService.register(TestUtils.getRegistrationData("test1@test.com", LOGIN1, PASSWORD));
        registrationService.register(TestUtils.getRegistrationData("test2@test.com", LOGIN2, PASSWORD));

        String token1 = TestUtils.login(LOGIN1, PASSWORD, port);
        String token2 = TestUtils.login(LOGIN2, PASSWORD, port);

        client1 = new TestWebSocketClient(token1, port);
        client2 = new TestWebSocketClient(token2, port);

        client1.connectBlocking();
        client2.connectBlocking();
    }

    @Test
    public void matchMakingTest() throws InvalidProtocolBufferException, InterruptedException {

        client1.send(TestUtils.buildFindGameMsgWrapped().toByteArray());
        client2.send(TestUtils.buildFindGameMsgWrapped().toByteArray());

        CommonProtos.WrapperMessage message1 = client1.blockingPoll();
        CommonProtos.WrapperMessage message2 = client2.blockingPoll();

        assertEquals("After successful matchmaking GameReady message is expected from server",
                CommonProtos.WrapperMessage.MessageCase.GAMEREADY, message1.getMessageCase());
        assertEquals("After successful matchmaking GameReady message is expected from server",
                CommonProtos.WrapperMessage.MessageCase.GAMEREADY, message2.getMessageCase());
    }

}