package nattypro.life.forum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendPasswordResetEmail(String toEmail, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("NattyPro.Life — Password Reset");
        message.setText(
            "Hi,\n\n" +
            "You requested a password reset for your NattyPro.Life account.\n\n" +
            "Click the link below to reset your password:\n" +
            baseUrl + "/reset-password?token=" + token + "\n\n" +
            "This link expires in 1 hour.\n\n" +
            "If you did not request this, ignore this email.\n\n" +
            "— The NattyPro.Life Team"
        );
        mailSender.send(message);
    }

    public void sendEmailConfirmation(String toEmail, String username, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("NattyPro.Life — Confirm Your Email");
        message.setText(
            "Welcome to NattyPro.Life, " + username + "!\n\n" +
            "Please confirm your email address by clicking the link below:\n" +
            baseUrl + "/confirm-email?token=" + token + "\n\n" +
            "This link expires in 24 hours.\n\n" +
            "If you did not create an account, ignore this email.\n\n" +
            "— The NattyPro.Life Team"
        );
        mailSender.send(message);
    }
}