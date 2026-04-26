package client.presentation.profile;

import client.presentation.login.main.Application;
import server.core.service.EmployeeService;
import com.formdev.flatlaf.FlatClientProperties;
import common.dto.EmployeeDTO;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class FormProfileInfo extends JPanel {

    private final EmployeeService employeeService;

    private JPanel panel;
    private JLabel lblTitle;
    private JLabel lblAvatar;

    private JTextField txtEmployeeID;
    private JTextField txtFullName;
    private JTextField txtGender;
    private JTextField txtPhone;
    private JTextField txtEmail;
    private JTextField txtTypeName;

    private BufferedImage bgImage;

    public FormProfileInfo(EmployeeService employeeService) {
        this.employeeService = employeeService;

        setLayout(new BorderLayout());
        initGUI();
        loadEmployeeInformation();
        add(panel, BorderLayout.CENTER);
    }

    private void initGUI() {
        panel = new JPanel(new MigLayout(
                "wrap 2, fillx, insets 22 70 22 70, gap 18 20",
                "[grow 0,trail]20[fill,grow]"
        ));
        panel.putClientProperty(FlatClientProperties.STYLE,
                "arc:18;background:#0F2A47;foreground:#EAF2FF;");

        lblTitle = new JLabel("Thông tin cá nhân");
        lblTitle.putClientProperty(FlatClientProperties.STYLE,
                "font:bold +6;foreground:#F2C94C");

        lblAvatar = new JLabel();
        lblAvatar.setHorizontalAlignment(SwingConstants.CENTER);
        lblAvatar.setPreferredSize(new Dimension(140, 140));

        txtEmployeeID = roField();
        txtFullName = roField();
        txtGender = roField();
        txtPhone = roField();
        txtEmail = roField();
        txtTypeName = roField();

        JPanel pnHeader = new JPanel(new MigLayout("wrap, insets 0 0 12 0", "[grow]", "[]10[]"));
        pnHeader.setOpaque(false);
        pnHeader.add(lblTitle, "al left");
        pnHeader.add(lblAvatar, "al center");

        panel.add(pnHeader, "span 2, growx, wrap");

        panel.add(label("EmployeeID:"));      panel.add(txtEmployeeID, "growx");
        panel.add(label("Họ tên:"));          panel.add(txtFullName, "growx");
        panel.add(label("Giới tính:"));       panel.add(txtGender, "growx");
        panel.add(label("Số điện thoại:"));   panel.add(txtPhone, "growx");
        panel.add(label("Email:"));           panel.add(txtEmail, "growx");
        panel.add(label("Loại nhân viên:"));  panel.add(txtTypeName, "growx");
    }

    private void loadEmployeeInformation() {
        try {
            String username = Application.session.getUsername();
            EmployeeDTO e = employeeService.getByUsername(username);

            if (e == null) {
                JOptionPane.showMessageDialog(this,
                        "Không tìm thấy thông tin nhân viên đang đăng nhập.",
                        "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            txtEmployeeID.setText(nvl(e.getEmployeeId()));
            txtFullName.setText(nvl(e.getFullName()));
            txtGender.setText(e.isGender() ? "Nam" : "Nữ");
            txtPhone.setText(nvl(e.getPhone()));
            txtEmail.setText(nvl(e.getEmail()));
            txtTypeName.setText(nvl(e.getEmployeeTypeName()));

            displayImage(e.getImgSource());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Không tải được thông tin cá nhân:\n" + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.putClientProperty(FlatClientProperties.STYLE,
                "foreground:#D9E6FF;font:+1");
        return l;
    }

    private JTextField roField() {
        JTextField t = new JTextField();
        t.setEditable(false);
        t.putClientProperty(FlatClientProperties.STYLE,
                "background:#12355A;foreground:#EAF2FF;borderWidth:0;arc:14;padding:10,14,10,14;font:+1");
        return t;
    }

    private String nvl(String v) {
        return v == null ? "" : v;
    }

    private void displayImage(String imagePath) {
        try {
            if (imagePath == null || imagePath.trim().isEmpty()) {
                lblAvatar.setIcon(null);
                return;
            }

            Image img = null;
            File f = new File(imagePath);
            if (f.exists()) {
                img = ImageIO.read(f);
            } else {
                java.net.URL u = getClass().getResource(imagePath.startsWith("/") ? imagePath : "/" + imagePath);
                if (u != null) img = ImageIO.read(u);
            }

            if (img != null) {
                Image scaled = img.getScaledInstance(140, 140, Image.SCALE_SMOOTH);
                lblAvatar.setIcon(new ImageIcon(scaled));
            } else {
                lblAvatar.setIcon(null);
            }
        } catch (Exception e) {
            lblAvatar.setIcon(null);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        if (bgImage != null) {
            g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
        } else {
            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(10, 34, 55),
                    getWidth(), getHeight(), new Color(20, 60, 100)
            );
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
        g2.dispose();
    }
}