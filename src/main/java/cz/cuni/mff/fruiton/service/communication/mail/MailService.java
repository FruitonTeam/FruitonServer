package cz.cuni.mff.fruiton.service.communication.mail;

import org.springframework.scheduling.annotation.Async;

public interface MailService {

    /**
     * Sends email to specified address with specified content.
     * @param to email address to which send an email
     * @param subject subject of the email
     * @param content content of the email
     */
    @Async
    void send(String to, String subject, String content);

}
