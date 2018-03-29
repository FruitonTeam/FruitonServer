package cz.cuni.mff.fruiton.config;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
@PropertySource("classpath:mail.properties")
public class EmailConfig {

    private static final Logger logger = Logger.getLogger(EmailConfig.class.getName());

    @Value("${mail.send.host}")
    private String host;

    @Value("${mail.send.port}")
    private int port;

    @Value("${mail.send.username}")
    private String username;

    @Value("${mail.send.password.file}")
    private File passwordFile;

    @Value("${mail.from}")
    private String from;

    @Bean
    public MailSender mailSender() {
        if (host == null || username == null || passwordFile == null) {
            logger.log(Level.WARNING, "Missing some mail parameters, sending of emails won't work");
            return new DummyMailSender();
        }
        String password;
        try {
            password = FileUtils.readFileToString(passwordFile, Charset.defaultCharset()).trim();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not read mail password, sending of emails won't work");
            return new DummyMailSender();
        }

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties properties = new Properties();
        properties.setProperty("mail.smtp.auth", Boolean.TRUE.toString());
        properties.setProperty("mail.smtp.starttls.enable", Boolean.TRUE.toString());

        mailSender.setJavaMailProperties(properties);

        return mailSender;
    }

    @Bean
    public SimpleMailMessage templateMessage() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from); // overridden by google when using google smtp
        return message;
    }

    private static final class DummyMailSender implements MailSender {

        private static final Logger logger = Logger.getLogger(DummyMailSender.class.getName());

        @Override
        public void send(final SimpleMailMessage simpleMailMessage) throws MailException {
            logger.log(Level.FINE, "Using dummy mail sender, message {0} won't be sent", simpleMailMessage);
        }

        @Override
        public void send(final SimpleMailMessage... simpleMailMessages) throws MailException {
            logger.log(Level.FINE, "Using dummy mail sender, messages {0} won't be sent",
                    Arrays.toString(simpleMailMessages));
        }
    }

}
