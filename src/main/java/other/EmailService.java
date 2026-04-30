package other;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    private static final String FROM_EMAIL = "baodinh.nguyen321@gmail.com";
    private static final String APP_PASSWORD = "kyfmzuezyxkvllat";

    public static void sendOtp(String toEmail, String otp) throws Exception {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Email nhận OTP không hợp lệ.");
        }

        Properties props = new Properties();

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL, "Mimosa Hotel"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Mã OTP đặt lại mật khẩu - Mimosa Hotel", "UTF-8");

        String html = """
                <div style="font-family: Arial, sans-serif; font-size: 14px;">
                    <h2>Mimosa Hotel</h2>
                    <p>Bạn đang yêu cầu đặt lại mật khẩu.</p>
                    <p>Mã OTP của bạn là:</p>
                    <h1 style="letter-spacing: 4px;">%s</h1>
                    <p>Mã OTP có hiệu lực trong 5 phút.</p>
                </div>
                """.formatted(otp);

        message.setContent(html, "text/html; charset=UTF-8");

        Transport.send(message);
    }
}