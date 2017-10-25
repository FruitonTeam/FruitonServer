package cz.cuni.mff.fruiton.service.communication.mail.impl;

import cz.cuni.mff.fruiton.service.communication.mail.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService {

    private final MailSender mailSender;

    private final SimpleMailMessage templateMessage;

    @Autowired
    public MailServiceImpl(final MailSender mailSender, final SimpleMailMessage templateMessage) {
        this.mailSender = mailSender;
        this.templateMessage = templateMessage;
    }

    @Override
    public final void send(final String to, final String subject, final String content) {
        SimpleMailMessage msg = new SimpleMailMessage(templateMessage);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(content);

        mailSender.send(msg);
    }

}
