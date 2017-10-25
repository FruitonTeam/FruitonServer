package cz.cuni.mff.fruiton.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@PropertySource("classpath:mail.properties")
public class EmailConfig {

    @Value("${mail.send.host}")
    private String host;

    @Value("${mail.send.port}")
    private int port;

    @Value("${mail.send.username}")
    private String username;

    @Value("${mail.send.password}")
    private String password;

    @Value("${mail.from}")
    private String from;

    @Bean
    public MailSender mailSender() {
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

}
