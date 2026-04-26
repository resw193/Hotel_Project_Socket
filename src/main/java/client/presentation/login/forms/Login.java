package client.presentation.login.forms;

import client.network.socket.HotelClient;
import client.network.socket.SocketRequestExecutor;
import client.presentation.login.main.Application;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.util.UIScale;
import common.dto.request_dto.LoginRequestDTO;
import common.dto.request_dto.LoginResultDTO;
import common.protocol.command.CommandType;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;
import net.miginfocom.swing.MigLayout;
import server.core.service.AccountService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;

public class Login extends JPanel {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnForgot;

    public Login() {
        init();
        txtUsername.addKeyListener(new EnterKeyListener());
        txtPassword.addKeyListener(new EnterKeyListener());
    }

    private void init() {
        setOpaque(false);
        setLayout(new MigLayout("wrap,fillx,insets 45 45 50 45", "[fill]"));

        JLabel title = new JLabel("Đăng nhập tài khoản của bạn", SwingConstants.CENTER);
        txtUsername = new JTextField();
        txtPassword = new JPasswordField();
        JCheckBox chRememberMe = new JCheckBox("Nhớ tài khoản");
        btnForgot = new JButton("Quên mật khẩu?");
        btnLogin = new JButton("Đăng nhập");

        title.putClientProperty(FlatClientProperties.STYLE, "font:bold +10");

        txtUsername.putClientProperty(
                FlatClientProperties.STYLE,
                "margin:5,10,5,10;focusWidth:1;innerFocusWidth:0"
        );
        txtPassword.putClientProperty(
                FlatClientProperties.STYLE,
                "margin:5,10,5,10;focusWidth:1;innerFocusWidth:0;showRevealButton:true"
        );

        btnLogin.putClientProperty(
                FlatClientProperties.STYLE,
                "background:$Component.accentColor;borderWidth:0;focusWidth:0;innerFocusWidth:0"
        );
        btnForgot.putClientProperty(
                FlatClientProperties.STYLE,
                "borderWidth:0;focusWidth:0;innerFocusWidth:0;foreground:$Component.accentColor"
        );

        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập tên đăng nhập");
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập mật khẩu");

        add(title);
        add(new JLabel("Tên đăng nhập"), "gapy 20");
        add(txtUsername);
        add(new JLabel("Mật khẩu"), "gapy 10");
        add(txtPassword);

        JPanel rowOptions = new JPanel(new MigLayout("insets 0, fillx", "[left]push[right]", "[]"));
        rowOptions.setOpaque(false);
        rowOptions.add(chRememberMe, "left");
        rowOptions.add(btnForgot, "right");
        add(rowOptions);

        add(btnLogin, "gapy 25");

        btnLogin.addActionListener(this::cmdLoginActionPerformed);
        btnForgot.addActionListener(this::cmdForgotActionPerformed);
    }

    private void cmdLoginActionPerformed(ActionEvent evt) {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword()).trim();

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Username và password không được trống.",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        try (
                HotelClient client = new HotelClient(Application.getSocketHost(), Application.getSocketPort());
                SocketRequestExecutor executor = new SocketRequestExecutor(client)
        ) {
            LoginRequestDTO loginRequestDTO = new LoginRequestDTO(user, pass);
            BaseRequest request = BaseRequest.of(CommandType.LOGIN, loginRequestDTO);

            BaseResponse response = executor.execute(request);

            if (!response.isSuccess()) {
                JOptionPane.showMessageDialog(
                        this,
                        response.getMessage(),
                        "Đăng nhập thất bại",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            Object data = response.getData();
            if (!(data instanceof LoginResultDTO loginResultDTO)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Dữ liệu trả về từ server không hợp lệ.",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            Application.loginSuccess(user, pass, loginResultDTO);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Đăng nhập thất bại: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void cmdForgotActionPerformed(ActionEvent evt) {
        AccountService accountService = Application.getAccountService();
        if (accountService == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "AccountService chưa được khởi tạo.",
                    "Lỗi hệ thống",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String username = JOptionPane.showInputDialog(
                this,
                "Nhập username để nhận OTP qua email:",
                "Quên mật khẩu",
                JOptionPane.QUESTION_MESSAGE
        );

        if (username == null) {
            return;
        }

        username = username.trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username không được trống");
            return;
        }

        try {
            String email = accountService.sendOtpForReset(username);
            JOptionPane.showMessageDialog(
                    this,
                    "Đã gửi mã OTP đến email: " + email + "\nVui lòng kiểm tra hộp thư và nhập OTP để đặt lại mật khẩu."
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Gửi OTP thất bại: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        JTextField txtOtp = new JTextField();
        JPasswordField txtNew = new JPasswordField();
        JPasswordField txtConfirm = new JPasswordField();

        txtOtp.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập OTP 6 số");
        txtNew.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Mật khẩu mới");
        txtConfirm.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Xác nhận mật khẩu");

        JPanel panel = new JPanel(new MigLayout("wrap 2,insets 10 10 5 10", "[][250!]"));
        panel.add(new JLabel("OTP:"));
        panel.add(txtOtp, "growx");
        panel.add(new JLabel("Mật khẩu mới:"));
        panel.add(txtNew, "growx");
        panel.add(new JLabel("Xác nhận:"));
        panel.add(txtConfirm, "growx");

        int opt = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Xác thực OTP & đặt mật khẩu mới",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (opt != JOptionPane.OK_OPTION) {
            return;
        }

        String enteredOtp = txtOtp.getText().trim();
        String newPass = new String(txtNew.getPassword()).trim();
        String confirm = new String(txtConfirm.getPassword()).trim();

        try {
            boolean ok = accountService.resetPasswordWithOtp(username, enteredOtp, newPass, confirm);
            JOptionPane.showMessageDialog(
                    this,
                    ok ? "Đổi mật khẩu thành công. Vui lòng đăng nhập lại!" : "Đổi mật khẩu thất bại"
            );
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Đổi mật khẩu thất bại: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arc = UIScale.scale(20);
        g2.setColor(new Color(11, 31, 51));
        g2.setComposite(AlphaComposite.SrcOver.derive(0.6f));
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), arc, arc));

        g2.dispose();
        super.paintComponent(g);
    }

    private final class EnterKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                cmdLoginActionPerformed(null);
            }
        }
    }
}