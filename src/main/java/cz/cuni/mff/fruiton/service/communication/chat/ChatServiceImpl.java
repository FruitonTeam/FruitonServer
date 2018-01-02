package cz.cuni.mff.fruiton.service.communication.chat;

import cz.cuni.mff.fruiton.chat.MessageStatus;
import cz.cuni.mff.fruiton.dao.UserIdHolder;
import cz.cuni.mff.fruiton.dao.domain.Message;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dao.repository.MessageRepository;
import cz.cuni.mff.fruiton.dao.repository.UserRepository;
import cz.cuni.mff.fruiton.dto.ChatProtos;
import cz.cuni.mff.fruiton.dto.CommonProtos.WrapperMessage;
import cz.cuni.mff.fruiton.service.communication.CommunicationService;
import cz.cuni.mff.fruiton.service.game.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatServiceImpl implements ChatService {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    private final CommunicationService communicationService;
    private final PlayerService playerService;

    @Autowired
    public ChatServiceImpl(
            final UserRepository userRepository,
            final MessageRepository messageRepository,
            final CommunicationService communicationService,
            final PlayerService playerService
    ) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.communicationService = communicationService;
        this.playerService = playerService;
    }

    @Override
    public final void accept(final UserIdHolder sender, final ChatProtos.ChatMessage message) {
        Message msgToPersist = new Message();
        msgToPersist.setSender(userRepository.findOne(sender.getId()));

        User recipient = userRepository.findByLogin(message.getRecipient());
        msgToPersist.setRecipient(recipient);

        msgToPersist.setContent(message.getMessage());

        messageRepository.save(msgToPersist);

        if (playerService.isOnline(UserIdHolder.of(recipient))) {
            communicationService.send(UserIdHolder.of(recipient), WrapperMessage.newBuilder().setChatMessage(message).build());
            msgToPersist.setStatus(MessageStatus.DELIVERED);
            messageRepository.save(msgToPersist);
        }
    }

}
