package cz.cuni.mff.fruiton.service.util.impl;

import cz.cuni.mff.fruiton.service.util.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

@Service
public class MailServiceImpl implements MailService {

    private final MailSender mailSender;

    private final SimpleMailMessage templateMessage;

    @Autowired
    public MailServiceImpl(MailSender mailSender, SimpleMailMessage templateMessage) {
        this.mailSender = mailSender;
        this.templateMessage = templateMessage;
    }

    @Override
    public void send(@Nonnull String to, @Nonnull String subject, @Nonnull String content) {
        SimpleMailMessage msg = new SimpleMailMessage(templateMessage);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(content);

        mailSender.send(msg);
    }

}
