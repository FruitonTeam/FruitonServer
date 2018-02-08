package cz.cuni.mff.fruiton.service.communication.chat;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dao.domain.Message;
import cz.cuni.mff.fruiton.dao.repository.MessageRepository;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.dto.ChatProtos;
import cz.cuni.mff.fruiton.service.authentication.AuthenticationService;
import cz.cuni.mff.fruiton.service.communication.CommunicationService;
import cz.cuni.mff.fruiton.service.communication.SessionService;
import cz.cuni.mff.fruiton.test.util.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = cz.cuni.mff.fruiton.Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ChatServiceImplTest {

    private ChatServiceImpl chatService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private CommunicationService communicationService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Mock
    private AuthenticationService authenticationService;

    @Before
    public void setup() {
        chatService = new ChatServiceImpl(
                userRepository,
                messageRepository,
                communicationService,
                sessionService,
                authenticationService,
                mongoTemplate);
    }

    @Test
    public void testGetMessagesBetweenUsers() {
        UserIdHolder user1 = TestUtils.createUser(userRepository, "user1");
        UserIdHolder user2 = TestUtils.createUser(userRepository, "user2");
        UserIdHolder user3 = TestUtils.createUser(userRepository, "user3");
        UserIdHolder user4 = TestUtils.createUser(userRepository, "user4");
        UserIdHolder user5 = TestUtils.createUser(userRepository, "user5");

        chatService.accept(user1, getChatMessage("user2", "testContent"));
        chatService.accept(user1, getChatMessage("user4", "testContent2"));
        chatService.accept(user4, getChatMessage("user3", "testContent3"));
        chatService.accept(user4, getChatMessage("user1", "testContent4"));

        ChatProtos.ChatMessages messages = chatService.getMessagesBetweenUsers(user1, user4, 0);

        assertThat(messages.getMessagesList().stream().map(ChatProtos.ChatMessage::getMessage).collect(Collectors.toList()),
                contains("testContent4", "testContent2"));
    }

    @Test
    public void testGetMessagesBefore() {
        UserIdHolder user1 = TestUtils.createUser(userRepository, "user1");
        UserIdHolder user2 = TestUtils.createUser(userRepository, "user2");

        chatService.accept(user1, getChatMessage("user2", "testContent"));
        chatService.accept(user1, getChatMessage("user2", "testContent2"));
        chatService.accept(user1, getChatMessage("user2", "testContent3"));

        List<Message> messageList = messageRepository.findAll();
        messageList.sort(Comparator.comparing(Message::getCreated));

        Message lastMessage = messageList.get(messageList.size() - 1);

        when(authenticationService.getLoggedInUser()).thenReturn(user1);

        ChatProtos.ChatMessages messages = chatService.getMessagesBefore(lastMessage.getId(), 0);

        assertThat(messages.getMessagesList().stream().map(ChatProtos.ChatMessage::getMessage).collect(Collectors.toList()),
                contains("testContent2", "testContent"));
    }

    private static ChatProtos.ChatMessage getChatMessage(final String recipient, final String content) {
        return ChatProtos.ChatMessage.newBuilder().setRecipient(recipient).setMessage(content).build();
    }

}