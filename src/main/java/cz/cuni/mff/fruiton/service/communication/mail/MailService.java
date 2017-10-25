package cz.cuni.mff.fruiton.service.communication.mail;

public interface MailService {

    void send(String to, String subject, String content);

}
