package cz.cuni.mff.fruiton.service.util;

public interface MailService {

    void send(String to, String subject, String content);

}
