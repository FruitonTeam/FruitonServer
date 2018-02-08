package cz.cuni.mff.fruiton.service.communication.chat;

import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dao.domain.Message;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dao.repository.MessageRepository;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.dto.ChatProtos;
import cz.cuni.mff.fruiton.dto.CommonProtos.WrapperMessage;
import cz.cuni.mff.fruiton.service.authentication.AuthenticationService;
import cz.cuni.mff.fruiton.service.communication.CommunicationService;
import cz.cuni.mff.fruiton.service.communication.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public final class ChatServiceImpl implements ChatService {

    private static final int MESSAGES_PAGE_SIZE = 50;

    private static final String SENDER_FIELD = "sender";
    private static final String RECIPIENT_FIELD = "recipient";
    private static final String CREATED_FIELD = "created";

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    private final CommunicationService communicationService;
    private final SessionService sessionService;

    private final AuthenticationService authService;

    private final MongoTemplate mongoTemplate;

    @Autowired
    public ChatServiceImpl(
            final UserRepository userRepository,
            final MessageRepository messageRepository,
            final CommunicationService communicationService,
            final SessionService sessionService,
            final AuthenticationService authService,
            final MongoTemplate mongoTemplate
    ) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.communicationService = communicationService;
        this.sessionService = sessionService;
        this.authService = authService;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void accept(final UserIdHolder sender, final ChatProtos.ChatMessage message) {
        Message msgToPersist = new Message();
        msgToPersist.setSender(userRepository.findOne(sender.getId()));

        User recipient = userRepository.findByLogin(message.getRecipient());
        msgToPersist.setRecipient(recipient);

        msgToPersist.setContent(message.getMessage());

        messageRepository.save(msgToPersist);

        if (sessionService.isOnline(UserIdHolder.of(recipient))) {
            communicationService.send(UserIdHolder.of(recipient), WrapperMessage.newBuilder().setChatMessage(
                    message.toBuilder()
                            .setId(msgToPersist.getId())
                            .setTimestamp(msgToPersist.getTimestamp()))
                    .build());
        }
    }

    @Override
    public ChatProtos.ChatMessages getMessagesBetweenUsers(
            final UserIdHolder user1,
            final UserIdHolder user2,
            final int page
    ) {
        Query query = new Query()
                .addCriteria(Criteria.where(SENDER_FIELD).in(user1.getId(), user2.getId()))
                .addCriteria(Criteria.where(RECIPIENT_FIELD).in(user1.getId(), user2.getId()))
                .with(new PageRequest(page, MESSAGES_PAGE_SIZE))
                .with(new Sort(Sort.Direction.DESC, CREATED_FIELD));

        List<ChatProtos.ChatMessage> messages = mongoTemplate.find(query, Message.class).stream()
                .map(Message::toProtobuf)
                .collect(Collectors.toList());

        return ChatProtos.ChatMessages.newBuilder().addAllMessages(messages).build();
    }

    @Override
    public ChatProtos.ChatMessages getMessagesBefore(final String messageId, final int page) {
        Message m = messageRepository.findOne(messageId);
        if (m == null) {
            throw new IllegalArgumentException("Cannot find message with id " + messageId);
        }

        UserIdHolder loggedInUser = authService.getLoggedInUser();
        if (!(loggedInUser.represents(m.getRecipient()) || loggedInUser.represents(m.getSender()))) {
            throw new SecurityException("User cannot access message with id " + messageId);
        }

        Query query = new Query()
                .addCriteria(Criteria.where(SENDER_FIELD).in(m.getSender().getId(), m.getRecipient().getId()))
                .addCriteria(Criteria.where(RECIPIENT_FIELD).in(m.getSender().getId(), m.getRecipient().getId()))
                .addCriteria(Criteria.where(CREATED_FIELD).lt(m.getCreated()))
                .with(new PageRequest(page, MESSAGES_PAGE_SIZE))
                .with(new Sort(Sort.Direction.DESC, CREATED_FIELD));

        List<ChatProtos.ChatMessage> messages = mongoTemplate.find(query, Message.class).stream()
                .map(Message::toProtobuf)
                .collect(Collectors.toList());

        return ChatProtos.ChatMessages.newBuilder().addAllMessages(messages).build();
    }

}
