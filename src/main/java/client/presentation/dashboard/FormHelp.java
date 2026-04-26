package client.presentation.dashboard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FormHelp extends JPanel {

    private static final Color BG     = new Color(0x0B1F33);
    private static final Color BORDER = new Color(0x274A6B);
    private static final Font  BASE_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    private String employeeName;
    private String employeeRole;

    public FormHelp() {
        this(null, null);
    }

    public FormHelp(String employeeName, String employeeRole) {
        this.employeeName = employeeName;
        this.employeeRole = employeeRole;

        setLayout(new BorderLayout());
        setBackground(BG);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(12, 12, 12, 12));

        wrapper.add(buildHeaderPanel(), BorderLayout.NORTH);
        wrapper.add(buildBodyPanel(), BorderLayout.CENTER);

        add(wrapper, BorderLayout.CENTER);
    }

    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG);
        header.setBorder(new EmptyBorder(10, 18, 12, 18));

        String name = (employeeName == null || employeeName.isBlank())
                ? ".........."
                : employeeName;
        String role = (employeeRole == null || employeeRole.isBlank())
                ? ".........."
                : employeeRole;

        // thông tin nhân viên
        JPanel infoLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        infoLine.setOpaque(false);

        JLabel lblEmp = new JLabel("Tên nhân viên: " + name);
        lblEmp.setFont(BASE_FONT.deriveFont(Font.PLAIN, 12f));
        lblEmp.setForeground(new Color(0xE5E7EB));

        JLabel sep = new JLabel("|");
        sep.setForeground(new Color(0x9CA3AF));

        JLabel lblRole = new JLabel("Chức vụ: " + role);
        lblRole.setFont(BASE_FONT.deriveFont(Font.PLAIN, 12f));
        lblRole.setForeground(new Color(0xE5E7EB));

        infoLine.add(lblEmp);
        infoLine.add(sep);
        infoLine.add(lblRole);

        // tiêu đề & mô tả
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("TRỢ GIÚP SỬ DỤNG HỆ THỐNG", SwingConstants.CENTER);
        lblTitle.setFont(BASE_FONT.deriveFont(Font.BOLD, 18f));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel(
                "Ứng dụng quản lý thông tin đặt phòng tại Khách sạn Mimosa",
                SwingConstants.CENTER
        );
        lblSub.setFont(BASE_FONT.deriveFont(Font.PLAIN, 12f));
        lblSub.setForeground(new Color(0xD1D5DB));

        titlePanel.add(lblTitle);
        titlePanel.add(lblSub);

        header.add(infoLine, BorderLayout.NORTH);
        header.add(titlePanel, BorderLayout.CENTER);

        return header;
    }

    private JPanel buildBodyPanel() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);

        JPanel card = new JPanel();
        card.setBackground(new Color(0x102A43));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(24, 28, 24, 28)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(
                "<html><div style='text-align:center;'>Hướng dẫn chi tiết cách sử dụng hệ thống " +
                        "được trình bày trong một trang web riêng.<br/>" +
                        "Nhấn nút bên dưới để mở hướng dẫn.</div></html>",
                SwingConstants.CENTER
        );
        lblTitle.setFont(BASE_FONT);
        lblTitle.setForeground(new Color(0xE5E7EB));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lblTitle);
        card.add(Box.createVerticalStrut(18));

        JButton btnOpen = new JButton("Xem hướng dẫn");
        stylePrimaryButton(btnOpen);
        btnOpen.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnOpen.addActionListener(e -> openUserGuide());

        card.add(btnOpen);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(16, 16, 16, 16);
        gbc.fill = GridBagConstraints.NONE;
        body.add(card, gbc);

        return body;
    }

    private void stylePrimaryButton(JButton b) {
        b.setFont(BASE_FONT.deriveFont(Font.BOLD, 14f));
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(0x2563EB));
        b.setBorder(new LineBorder(new Color(0x1D4ED8), 1, true));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(200, 38));
    }

    // Mở file HTML hướng dẫn trong web
    private void openUserGuide() {
        final String HTML_RESOURCE = "/data/help/huong-dan-su-dung.html";
        final String IMAGE_FOLDER  = "/data/help/img";     // thư mục chứa toàn bộ ảnh

        try {
            // 1. Tạo thư mục tạm
            Path tempDir = Files.createTempDirectory("Mimosa_Help_");
            Path htmlPath = tempDir.resolve("huong-dan-su-dung.html");

            // 2. Copy file HTML ra thư mục tạm
            try (InputStream in = getClass().getResourceAsStream(HTML_RESOURCE)) {
                if (in == null) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Không tìm thấy file hướng dẫn trong ứng dụng.\n" +
                                    "Đường dẫn resource: " + HTML_RESOURCE,
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
                Files.copy(in, htmlPath, StandardCopyOption.REPLACE_EXISTING);
            }

            // 3. Copy toàn bộ ảnh trong /data.help/img ra tempDir/img
            Path imgDir = tempDir.resolve("img");
            copyAllHelpImages(IMAGE_FOLDER, imgDir);

            // 4. Mở HTML trong browser
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(htmlPath.toUri());
            }
            else {
                JOptionPane.showMessageDialog(
                        this,
                        "Máy không hỗ trợ mở trình duyệt tự động.\n" +
                                "Bạn hãy mở file:\n" + htmlPath.toAbsolutePath(),
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể mở hướng dẫn:\n" + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void copyAllHelpImages(String resourceFolder, Path targetDir) throws IOException {
        Files.createDirectories(targetDir);

        java.net.URL dirURL = getClass().getResource(resourceFolder);
        if (dirURL == null) {
            System.err.println("Không tìm thấy folder resource: " + resourceFolder);
            return;
        }

        String protocol = dirURL.getProtocol();

        if ("file".equals(protocol)) {
            try {
                java.nio.file.Path sourceDir = java.nio.file.Paths.get(dirURL.toURI());
                try (java.util.stream.Stream<java.nio.file.Path> paths = java.nio.file.Files.list(sourceDir)) {
                    paths
                            .filter(java.nio.file.Files::isRegularFile)
                            .forEach(path -> {
                                String fileName = path.getFileName().toString();
                                try (InputStream in = java.nio.file.Files.newInputStream(path)) {
                                    Files.copy(in, targetDir.resolve(fileName),
                                            StandardCopyOption.REPLACE_EXISTING);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                }
            } catch (java.net.URISyntaxException e) {
                throw new IOException(e);
            }

        }
        else if ("jar".equals(protocol)) {
            String path = dirURL.getPath();
            String jarPath = path.substring("file:".length(), path.indexOf("!"));

            try (java.util.jar.JarFile jar = new java.util.jar.JarFile(
                    java.net.URLDecoder.decode(jarPath, java.nio.charset.StandardCharsets.UTF_8))) {

                String folder = resourceFolder.startsWith("/") ? resourceFolder.substring(1) : resourceFolder;
                if (!folder.endsWith("/")) folder += "/";

                java.util.Enumeration<java.util.jar.JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    java.util.jar.JarEntry entry = entries.nextElement();
                    String name = entry.getName();

                    if (entry.isDirectory() || !name.startsWith(folder)) continue;

                    String fileName = name.substring(folder.length());
                    try (InputStream in = jar.getInputStream(entry)) {
                        Files.copy(in, targetDir.resolve(fileName),
                                StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }

        } else {
            System.err.println("Protocol không hỗ trợ: " + protocol);
        }
    }

}
