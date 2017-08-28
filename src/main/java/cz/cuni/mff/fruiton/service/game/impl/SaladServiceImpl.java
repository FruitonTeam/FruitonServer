package cz.cuni.mff.fruiton.service.game.impl;

import cz.cuni.mff.fruiton.dao.domain.Fruiton;
import cz.cuni.mff.fruiton.dao.domain.User;
import cz.cuni.mff.fruiton.dto.GameProtos;
import cz.cuni.mff.fruiton.service.communication.CommunicationService;
import cz.cuni.mff.fruiton.service.game.SaladService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class SaladServiceImpl implements SaladService {

    private static final Logger logger = Logger.getLogger(SaladServiceImpl.class.getName());

    private final CommunicationService communicationService;

    @Autowired
    public SaladServiceImpl(final CommunicationService communicationService) {
        this.communicationService = communicationService;
    }

    @Override
    public final void sendSalad(final User user) {

        logger.log(Level.FINEST, "Sending salad info to user: {0}", user);

        List<GameProtos.Fruiton> saladDto = new LinkedList<>();
        if (user.getSalad() != null) {
            for (Fruiton fruiton : user.getSalad()) {
                    saladDto.add(fruiton.convertToDTO());
            }
        }

        GameProtos.WrapperMessage m = GameProtos.WrapperMessage.newBuilder()
                .setSalad(GameProtos.SaladInfo.newBuilder()
                        .addAllSalad(saladDto)
                        .build())
                .build();

        communicationService.send(user, m);
    }

}
