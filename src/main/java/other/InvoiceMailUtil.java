package other;



import common.entity.Customer;
import common.entity.Order;
import common.entity.OrderDetailRoom;
import common.entity.OrderDetailService;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import javax.mail.*;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class InvoiceMailUtil {

    private static final String FROM_EMAIL    = "baodinh.nguyen321@gmail.com";
    private static final String FROM_PASSWORD = "kyfmzuezyxkvllat";  // Gmail App Password
    private static final String HOTEL_NAME    = "Mimosa Hotel";

    private static Session createSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, FROM_PASSWORD);
            }
        });
    }

    public static void sendInvoiceEmail(Order order, List<OrderDetailRoom> roomLines, List<OrderDetailService> serviceLines, double totalAmount, double cashReceived)
            throws Exception {

        if (order == null) return;
        Customer c = order.getCustomer();
        if (c == null) return;
        String toEmail = c.getEmail();
        if (toEmail == null || toEmail.isBlank()) return;

        Session session = createSession();

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL, HOTEL_NAME));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("[Mimosa Hotel] Hóa đơn " + order.getOrderId());

        String html = buildHtmlBody(order, roomLines, serviceLines, totalAmount, cashReceived);
        message.setContent(html, "text/html; charset=UTF-8");

        //Transport.send(message);
    }

    private static String buildHtmlBody(
            Order order,
            List<OrderDetailRoom> roomLines,
            List<OrderDetailService> serviceLines,
            double totalAmount,
            double cashReceived
    ) {
        NumberFormat money = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        String customerName = order.getCustomer() != null ? nullToEmpty(order.getCustomer().getFullName()) : "Khách lẻ";
        String customerPhone = order.getCustomer() != null ? nullToEmpty(order.getCustomer().getPhone()) : "-";

        String orderDateStr = order.getOrderDate() != null
                ? order.getOrderDate().format(dtf)
                : "";

        double roomsSubtotal = 0d;
        double servicesSubtotal = 0d;

        StringBuilder sb = new StringBuilder();

        sb.append("<div style=\"font-family:'Segoe UI',Arial,sans-serif;font-size:13px;color:#111827;")
                .append("background-color:#F3F4F6;padding:24px;\">");

        sb.append("<div style=\"max-width:720px;margin:0 auto;background:#FFFFFF;border-radius:12px;")
                .append("box-shadow:0 10px 30px rgba(15,23,42,0.18);padding:24px 28px;\">");

        // Tiêu đề + tên KS
        sb.append("<div style=\"display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:12px;\">");

        sb.append("<div>")
                .append("<div style=\"font-size:22px;font-weight:700;color:#111827;margin-bottom:4px;\">HÓA ĐƠN THANH TOÁN</div>")
                .append("<div style=\"font-size:14px;font-weight:600;color:#2563EB;\">")
                .append(HOTEL_NAME)
                .append("</div>")
                .append("</div>");

        sb.append("<div style=\"text-align:right;font-size:12px;color:#6B7280;\">")
                .append("<div style=\"font-weight:600;color:#111827;\">Mã hóa đơn: ")
                .append(nullToEmpty(order.getOrderId()))
                .append("</div>")
                .append("<div>Ngày lập: ")
                .append(orderDateStr)
                .append("</div>")
                .append("<div>Thành phố Hồ Chí Minh</div>")
                .append("</div>");

        sb.append("</div>");

        // Thông tin khách hàng
        sb.append("<div style=\"display:flex;justify-content:space-between;font-size:13px;")
                .append("background:#F9FAFB;border-radius:8px;padding:12px 14px;margin-bottom:18px;\">");

        sb.append("<div>")
                .append("<div><span style=\"font-weight:600;color:#111827;\">Khách hàng: </span>")
                .append(customerName)
                .append("</div>")
                .append("<div><span style=\"font-weight:600;color:#111827;\">Số điện thoại: </span>")
                .append(customerPhone)
                .append("</div>")
                .append("</div>");

        sb.append("<div style=\"text-align:right;\">")
                .append("<div><span style=\"font-weight:600;color:#111827;\">Nhân viên lập: </span>")
                .append(order.getEmployee() != null ? nullToEmpty(order.getEmployee().getFullName()) : "-")
                .append("</div>")
                .append("<div><span style=\"font-weight:600;color:#111827;\">Mã nhân viên: </span>")
                .append(order.getEmployee() != null ? nullToEmpty(order.getEmployee().getEmployeeId()) : "-")
                .append("</div>")
                .append("</div>");

        sb.append("</div>");

        sb.append("<h3 style=\"font-size:15px;font-weight:600;color:#111827;margin:8px 0 6px;\">Các phòng đã đặt</h3>");

        if (roomLines != null && !roomLines.isEmpty()) {
            sb.append("<table style=\"width:100%;border-collapse:collapse;font-size:12px;margin-bottom:10px;\">");
            sb.append("<thead>")
                    .append("<tr>")
                    .append("<th style=\"text-align:left;padding:8px;border-bottom:1px solid #E5E7EB;\">Phòng</th>")
                    .append("<th style=\"text-align:left;padding:8px;border-bottom:1px solid #E5E7EB;\">Loại</th>")
                    .append("<th style=\"text-align:left;padding:8px;border-bottom:1px solid #E5E7EB;\">Nhận</th>")
                    .append("<th style=\"text-align:left;padding:8px;border-bottom:1px solid #E5E7EB;\">Trả</th>")
                    .append("<th style=\"text-align:left;padding:8px;border-bottom:1px solid #E5E7EB;\">Kiểu đặt</th>")
                    .append("<th style=\"text-align:right;padding:8px;border-bottom:1px solid #E5E7EB;\">Thành tiền</th>")
                    .append("</tr>")
                    .append("</thead><tbody>");

            for (OrderDetailRoom r : roomLines) {
                String roomID = (r.getRoom() != null) ? nullToEmpty(r.getRoom().getRoomId()) : "";
                String roomType = (r.getRoom() != null && r.getRoom().getRoomType() != null)
                        ? nullToEmpty(r.getRoom().getRoomType().getTypeName())
                        : "";
                String in = (r.getCheckInDate() != null) ? r.getCheckInDate().format(dtf) : "";
                String out = (r.getCheckOutDate() != null) ? r.getCheckOutDate().format(dtf) : "";
                String bookingType = nullToEmpty(r.getBookingType().getDisplayName());

                double fee = r.getRoomFee();
                if (fee <= 0) {
                    fee = calculateRoomFee(r);
                }
                roomsSubtotal += fee;

                sb.append("<tr>")
                        .append("<td style=\"padding:6px 8px;border-bottom:1px solid #F3F4F6;\">")
                        .append(roomID)
                        .append("</td>")
                        .append("<td style=\"padding:6px 8px;border-bottom:1px solid #F3F4F6;\">")
                        .append(roomType)
                        .append("</td>")
                        .append("<td style=\"padding:6px 8px;border-bottom:1px solid #F3F4F6;\">")
                        .append(in)
                        .append("</td>")
                        .append("<td style=\"padding:6px 8px;border-bottom:1px solid #F3F4F6;\">")
                        .append(out)
                        .append("</td>")
                        .append("<td style=\"padding:6px 8px;border-bottom:1px solid #F3F4F6;\">")
                        .append(bookingType)
                        .append("</td>")
                        .append("<td style=\"padding:6px 8px;border-bottom:1px solid #F3F4F6;text-align:right;\">")
                        .append(money.format(fee))
                        .append("</td>")
                        .append("</tr>");
            }

            sb.append("</tbody></table>");
        } else {
            sb.append("<div style=\"font-size:12px;color:#6B7280;margin-bottom:8px;\">Không có phòng.</div>");
        }

        // ===== Chi tiết dịch vụ (gộp + tính lại total giống hóa đơn giấy) =====
        if (serviceLines != null && !serviceLines.isEmpty()) {
            sb.append("<h3 style=\"font-size:15px;font-weight:600;color:#111827;margin:10px 0 6px;\">Dịch vụ đã sử dụng</h3>");

            // Gộp theo tên dịch vụ (logic y chang hóa đơn giấy)
            Map<String, ServiceAgg> agg = new LinkedHashMap<>();
            for (OrderDetailService s : serviceLines) {
                String name = (s.getService() == null)
                        ? "Dịch vụ"
                        : nullToEmpty(s.getService().getServiceName());
                int quantity = Math.max(0, s.getQuantity());
                double unit = (s.getService() != null)
                        ? s.getService().getPrice()
                        : 0d;

                ServiceAgg a = agg.computeIfAbsent(name, k -> new ServiceAgg());
                a.qty += quantity;
                a.unit = unit;
                a.total += unit * quantity;
            }

            sb.append("<table style=\"width:100%;border-collapse:collapse;font-size:12px;margin-bottom:10px;\">");
            sb.append("<thead>")
                    .append("<tr>")
                    .append("<th style=\"text-align:left;padding:8px;border-bottom:1px solid #E5E7EB;\">Dịch vụ</th>")
                    .append("<th style=\"text-align:center;padding:8px;border-bottom:1px solid #E5E7EB;\">SL</th>")
                    .append("<th style=\"text-align:right;padding:8px;border-bottom:1px solid #E5E7EB;\">Đơn giá</th>")
                    .append("<th style=\"text-align:right;padding:8px;border-bottom:1px solid #E5E7EB;\">Thành tiền</th>")
                    .append("</tr>")
                    .append("</thead><tbody>");

            for (Map.Entry<String, ServiceAgg> entry : agg.entrySet()) {
                String name = entry.getKey();
                ServiceAgg a = entry.getValue();
                servicesSubtotal += a.total;

                sb.append("<tr>")
                        .append("<td style=\"padding:6px 8px;border-bottom:1px solid #F3F4F6;\">")
                        .append(name)
                        .append("</td>")
                        .append("<td style=\"padding:6px 8px;border-bottom:1px solid #F3F4F6;text-align:center;\">")
                        .append(a.qty)
                        .append("</td>")
                        .append("<td style=\"padding:6px 8px;border-bottom:1px solid #F3F4F6;text-align:right;\">")
                        .append(money.format(a.unit))
                        .append("</td>")
                        .append("<td style=\"padding:6px 8px;border-bottom:1px solid #F3F4F6;text-align:right;\">")
                        .append(money.format(a.total))
                        .append("</td>")
                        .append("</tr>");
            }

            sb.append("</tbody></table>");
        }

        double change = Math.max(0, cashReceived - totalAmount);

        sb.append("<div style=\"border-top:1px solid #E5E7EB;margin-top:10px;padding-top:10px;font-size:13px;\">");

        sb.append("<div style=\"display:flex;justify-content:flex-end;margin-bottom:4px;\">")
                .append("<div style=\"min-width:200px;\">")
                .append("<div style=\"display:flex;justify-content:space-between;margin-bottom:2px;\">")
                .append("<span style=\"color:#4B5563;\">Tổng phòng & dịch vụ:</span>")
                .append("<span style=\"font-weight:600;\">")
                .append(money.format(roomsSubtotal + servicesSubtotal))
                .append("</span>")
                .append("</div>");

        sb.append("<div style=\"display:flex;justify-content:space-between;margin-bottom:2px;\">")
                .append("<span style=\"color:#4B5563;\">Tổng thanh toán (sau KM):</span>")
                .append("<span style=\"font-weight:600;\">")
                .append(money.format(totalAmount))
                .append("</span>")
                .append("</div>");

        sb.append("<div style=\"display:flex;justify-content:space-between;margin-bottom:2px;\">")
                .append("<span style=\"color:#4B5563;\">Tiền khách đưa:</span>")
                .append("<span style=\"font-weight:600;\">")
                .append(money.format(cashReceived))
                .append("</span>")
                .append("</div>");

        sb.append("<div style=\"display:flex;justify-content:space-between;\">")
                .append("<span style=\"color:#4B5563;\">Tiền thừa:</span>")
                .append("<span style=\"font-weight:600;\">")
                .append(money.format(change))
                .append("</span>")
                .append("</div>");

        sb.append("</div>");
        sb.append("</div>");

        // Footer
        sb.append("<div style=\"margin-top:16px;font-size:12px;color:#6B7280;text-align:center;\">")
                .append("Cảm ơn quý khách đã sử dụng dịch vụ của ")
                .append(HOTEL_NAME)
                .append(".<br>")
                .append("Nếu có bất kỳ thắc mắc nào về hóa đơn, vui lòng liên hệ lễ tân để được hỗ trợ.")
                .append("</div>");

        sb.append("</div>");
        sb.append("</div>");

        return sb.toString();
    }


    private static class ServiceAgg {
        int qty;
        double unit;
        double total;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private static double calculateRoomFee(OrderDetailRoom r) {
        try {
            if (r == null || r.getRoom() == null || r.getRoom().getRoomType() == null) {
                return 0d;
            }

            LocalDateTime in  = r.getCheckInDate();
            LocalDateTime out = r.getCheckOutDate();
            if (in == null || out == null || !out.isAfter(in)) {
                return 0d;
            }

            long duration = Duration.between(in, out).toHours();
            if (duration <= 0) return 0d;

            var rt = r.getRoom().getRoomType();
            String typeName = nvl(rt.getTypeName());
            double pricePerHour   = rt.getPricePerHour();
            double pricePerNight  = rt.getPricePerNight();
            double pricePerDay    = rt.getPricePerDay();
            double lateFeePerHour = rt.getLateFeePerHour();

            String booking = nvl(r.getBookingType().getDisplayName());
            double price;

            if (booking.equals("Giờ")) {
                double hourlyIncrement = typeName.equals("Phòng đơn") ? 10000d : 20000d;

                if (duration <= 1) {
                    price = pricePerHour;
                } else {
                    price = pricePerHour + (duration - 1) * hourlyIncrement;
                }
            }
            else if (booking.equals("Đêm")) {
                int durationHours = (int) duration;
                int nights;
                int holdHours;

                if (durationHours <= 13) {
                    nights    = 1;
                    holdHours = 0;
                } else {
                    nights = 1 + (durationHours - 13) / 24;

                    int baseHours = 13 + (nights - 1) * 24;
                    holdHours = (nights - 1) * 11;

                    if (durationHours > baseHours) {
                        holdHours += (durationHours - baseHours);
                    }
                }

                price = pricePerNight * nights
                        + (double) holdHours * lateFeePerHour;
            }
            else if (booking.equals("Ngày")) {
                price = pricePerDay;

                if (duration > 24) {
                    double lateFeeRate =
                            typeName.equals("Phòng đơn") ? 20000d : 30000d;
                    price += (duration - 24) * lateFeeRate;
                }
            }
            else {
                price = r.getRoomFee();
            }

            return price;

        } catch (Exception ex) {
            return Math.max(0d, r.getRoomFee());
        }
    }

}
