package cz.cuni.mff.fruiton.service.communication.mail;

import org.springframework.scheduling.annotation.Async;

public interface MailService {

    @Async
    void send(String to, String subject, String content);

}
