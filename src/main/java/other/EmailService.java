package other;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class EmailService {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;

    private static final String SENDER_EMAIL = System.getenv().getOrDefault("MAIL_USER", "baodinh.nguyen321@gmail.com");
    private static final String SENDER_NAME  = "Mimosa Hotel";
    private static final String APP_PASSWORD = System.getenv().getOrDefault("MAIL_APP_PASSWORD", "kyfmzuezyxkvllat");

    private static final String HOTEL_LOGO_PATH = "src/main/resources/images/mimosa_hotel_logo.jpg";

    public static void sendOtp(String toEmail, String otp) throws MessagingException, UnsupportedEncodingException {

        Session session = createSession();

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(SENDER_EMAIL, SENDER_NAME));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        msg.setSubject("[Mimosa Hotel] Mã OTP đặt lại mật khẩu", "UTF-8");

        String html = buildOtpHtmlBody(otp);

        MimeMultipart multipart = new MimeMultipart("related");

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(html, "text/html; charset=UTF-8");
        multipart.addBodyPart(htmlPart);

        try {
            File logoFile = new File(HOTEL_LOGO_PATH);
            if (logoFile.exists() && logoFile.isFile()) {
                MimeBodyPart logoPart = new MimeBodyPart();
                DataSource fds = new FileDataSource(logoFile);
                logoPart.setDataHandler(new DataHandler(fds));
                logoPart.setHeader("Content-ID", "<hotelLogo>");
                logoPart.setDisposition(MimeBodyPart.INLINE);
                multipart.addBodyPart(logoPart);
            } else {
                System.err.println("Không tìm thấy file logo: " + logoFile.getAbsolutePath()
                        + " -> Gửi email OTP không kèm logo.");
            }
        } catch (Exception ex) {
            System.err.println("Không thể đính kèm logo vào email OTP: " + ex.getMessage());
        }

        msg.setContent(multipart);

        Transport.send(msg);
    }

    private static Session createSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(SMTP_PORT));
        props.put("mail.smtp.ssl.trust", SMTP_HOST);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
            }
        });
    }

    private static String buildOtpHtmlBody(String otp) {
        StringBuilder sb = new StringBuilder();

        sb.append("<!DOCTYPE html>")
                .append("<html lang=\"vi\">")
                .append("<head>")
                .append("<meta charset=\"UTF-8\">")
                .append("<title>Mã OTP đặt lại mật khẩu - Mimosa Hotel</title>")
                .append("</head>")
                .append("<body style=\"margin:0;padding:0;background-color:#f4f4f4;")
                .append("font-family:'Segoe UI',Arial,sans-serif;\">")

                .append("<table align=\"center\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" ")
                .append("style=\"padding:24px 0;\">")
                .append("<tr><td>")
                .append("<table align=\"center\" width=\"600\" cellpadding=\"0\" cellspacing=\"0\" ")
                .append("style=\"background-color:#ffffff;border-radius:8px;overflow:hidden;")
                .append("box-shadow:0 4px 12px rgba(0,0,0,0.08);\">")

                .append("<tr><td style=\"background:linear-gradient(135deg,#142850,#27496d);")
                .append("padding:18px 24px;color:#ffffff;\">")
                .append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">")
                .append("<tr>")
                .append("<td style=\"width:64px;\" align=\"left\">")
                .append("<img src=\"cid:hotelLogo\" alt=\"Mimosa Hotel\" ")
                .append("style=\"width:56px;height:56px;border-radius:12px;display:block;\"/>")
                .append("</td>")
                .append("<td align=\"right\">")
                .append("<div style=\"font-size:18px;font-weight:600;\">Mimosa Hotel</div>")
                .append("<div style=\"font-size:12px;opacity:0.85;\">Hệ thống quản lý đặt phòng</div>")
                .append("</td>")
                .append("</tr></table>")
                .append("</td></tr>")

                .append("<tr><td style=\"padding:24px 24px 8px 24px;font-size:14px;color:#333333;\">")
                .append("<p style=\"margin:0 0 12px 0;\">Kính gửi Quý khách,</p>")
                .append("<p style=\"margin:0 0 12px 0;\">")
                .append("Chúng tôi đã nhận được yêu cầu <strong>đặt lại mật khẩu</strong> ")
                .append("cho tài khoản Mimosa Hotel của bạn.")
                .append("</p>")
                .append("<p style=\"margin:0 0 16px 0;\">")
                .append("Vui lòng sử dụng mã xác thực (OTP) dưới đây để hoàn tất bước xác minh:")
                .append("</p>")
                .append("</td></tr>")

                .append("<tr><td align=\"center\" style=\"padding:8px 24px 20px 24px;\">")
                .append("<div style=\"display:inline-block;padding:14px 24px;border-radius:10px;")
                .append("background-color:#142850;color:#ffffff;letter-spacing:3px;")
                .append("font-size:24px;font-weight:700;")
                .append("font-family:'Consolas','Courier New',monospace;\">")
                .append(otp)
                .append("</div>")
                .append("<div style=\"margin-top:12px;font-size:12px;color:#666666;\">")
                .append("Mã OTP có hiệu lực trong <strong>5 phút</strong> ")
                .append("và chỉ sử dụng <strong>1 lần</strong>.")
                .append("</div>")
                .append("</td></tr>")

                .append("<tr><td style=\"padding:0 24px 8px 24px;font-size:12px;color:#555555;\">")
                .append("<p style=\"margin:0 0 8px 0;\">")
                .append("Nếu bạn <strong>không thực hiện</strong> yêu cầu này, ")
                .append("vui lòng bỏ qua email hoặc liên hệ bộ phận hỗ trợ của khách sạn để được kiểm tra.")
                .append("</p>")
                .append("</td></tr>")

                .append("<tr><td style=\"padding:0 24px 18px 24px;font-size:12px;color:#555555;\">")
                .append("Trân trọng,<br/>")
                .append("<strong>Mimosa Hotel</strong><br/>")
                .append("<span style=\"color:#888888;\">Hệ thống quản lý đặt phòng</span>")
                .append("</td></tr>")

                .append("<tr><td style=\"background-color:#f5f7fb;padding:12px 24px;")
                .append("font-size:10px;color:#888888;text-align:center;\">")
                .append("Đây là email tự động, vui lòng không trả lời lại email này.")
                .append("</td></tr>")

                .append("</table>")
                .append("</td></tr>")
                .append("</table>")

                .append("</body></html>");

        return sb.toString();
    }
}
