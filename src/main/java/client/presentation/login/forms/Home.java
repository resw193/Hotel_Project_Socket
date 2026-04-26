package client.presentation.login.forms;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class Home extends JPanel {

    private final List<ModelLocation> locations = new ArrayList<>();
    private int index = 0;
    private HomeOverlay homeOverlay;

    private MediaPlayerFactory factory;
    private EmbeddedMediaPlayer mediaPlayer;
    private Canvas videoCanvas;

    public Home() {
        init();
        seedLocations();
    }

    private void init() {
        setLayout(new BorderLayout());
        initMediaPlayer();
    }

    private void initMediaPlayer() {
        try {
            factory = new MediaPlayerFactory();
            mediaPlayer = factory.mediaPlayers().newEmbeddedMediaPlayer();

            videoCanvas = new Canvas();
            videoCanvas.setBackground(Color.BLACK);

            mediaPlayer.videoSurface().set(factory.videoSurfaces().newVideoSurface(videoCanvas));
            mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
                @Override
                public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
                    long length = mediaPlayer.status().length();
                    if (length > 0 && newTime >= length - 1000) {
                        mediaPlayer.controls().setPosition(0f);
                    }
                }
            });

            add(videoCanvas, BorderLayout.CENTER);
        } catch (Exception ex) {
            // Nếu VLCJ/VLC native chưa sẵn sàng thì fallback nền đen
            removeAll();
            JPanel fallback = new JPanel(new BorderLayout());
            fallback.setBackground(new Color(11, 31, 51));

            JLabel label = new JLabel(
                    "<html><div style='text-align:center;'>"
                            + "Không khởi tạo được video background.<br>"
                            + "Hãy kiểm tra VLC/VLCJ và resource video."
                            + "</div></html>",
                    SwingConstants.CENTER
            );
            label.setForeground(Color.WHITE);
            fallback.add(label, BorderLayout.CENTER);

            add(fallback, BorderLayout.CENTER);
            revalidate();
            repaint();
        }
    }

    private void seedLocations() {
        locations.add(new ModelLocation(
                "KHÁCH SẠN MIMOSA",
                "Khách sạn MIMOSA mang đến không gian nghỉ dưỡng sang trọng cùng trải nghiệm mở đầu bằng video nền đậm chất thương hiệu.",
                "/video/video 1.mp4"
        ));

        locations.add(new ModelLocation(
                "Lời thì thầm từ những con sóng",
                "Chỉ cách biển vài bước chân, MIMOSA là nơi lý tưởng để tận hưởng kỳ nghỉ với không gian thư giãn và cảm giác gần gũi thiên nhiên.",
                "/video/video 2.mp4"
        ));

        locations.add(new ModelLocation(
                "Khung cảnh nên thơ",
                "Không gian ấm cúng, góc nhìn đẹp và sự thư thái là những gì MIMOSA muốn mang đến ngay từ màn hình chào đầu tiên.",
                "/video/video 3.mp4"
        ));
    }

    public void initOverlay(JFrame frame) {
        if (homeOverlay != null) {
            homeOverlay.dispose();
        }

        homeOverlay = new HomeOverlay(frame, locations);
        homeOverlay.getOverlay().setEventHomeOverlay(this::play);

        if (!locations.isEmpty()) {
            homeOverlay.getOverlay().setIndex(index);
        }

        if (mediaPlayer != null) {
            mediaPlayer.overlay().set(homeOverlay);
            mediaPlayer.overlay().enable(true);
        }
    }

    public void play(int index) {
        if (index < 0 || index >= locations.size()) {
            return;
        }

        this.index = index;
        ModelLocation location = locations.get(index);

        if (homeOverlay != null) {
            homeOverlay.getOverlay().setIndex(index);
        }

        if (mediaPlayer == null) {
            return;
        }

        try {
            if (mediaPlayer.status().isPlaying()) {
                mediaPlayer.controls().stop();
            }

            String mediaPath = resolveMediaFromResources(location.getVideoPath());
            if (mediaPath == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Không tìm thấy video: " + location.getVideoPath(),
                        "Lỗi media",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            mediaPlayer.media().play(mediaPath);
            mediaPlayer.controls().play();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể phát video nền.\nChi tiết: " + ex.getMessage(),
                    "Lỗi VLCJ",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public void disableOverlay() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.overlay().enable(false);
                mediaPlayer.overlay().set(null);
            } catch (Exception ignored) {
            }
        }

        if (homeOverlay != null) {
            try {
                homeOverlay.setVisible(false);
                homeOverlay.dispose();
            } catch (Exception ignored) {

            } finally {
                homeOverlay = null;
            }
        }
    }

    public void enableOverlay(JFrame owner) {
        if (homeOverlay == null) {
            initOverlay(owner);
        }

        if (mediaPlayer != null) {
            mediaPlayer.overlay().set(homeOverlay);
            mediaPlayer.overlay().enable(true);
        }

        if (homeOverlay != null) {
            homeOverlay.setVisible(true);
            homeOverlay.getOverlay().setIndex(index);
        }
    }

    public void stop() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.controls().stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception ignored) {
        }

        try {
            if (factory != null) {
                factory.release();
                factory = null;
            }
        } catch (Exception ignored) {
        }

        if (homeOverlay != null) {
            homeOverlay.setVisible(false);
            homeOverlay.dispose();
            homeOverlay = null;
        }
    }

    public List<ModelLocation> getLocations() {
        return locations;
    }

    private String resolveMediaFromResources(String resourcePath) {
        try {
            URL url = getClass().getResource(resourcePath);
            if (url == null) {
                return null;
            }

            if ("file".equalsIgnoreCase(url.getProtocol())) {
                try {
                    return Paths.get(url.toURI()).toString();
                } catch (Exception ex) {
                    return url.toExternalForm();
                }
            }

            String suffix = ".mp4";
            int dot = resourcePath.lastIndexOf('.');
            if (dot >= 0) {
                suffix = resourcePath.substring(dot);
            }

            try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
                if (in == null) {
                    return null;
                }

                Path temp = Files.createTempFile("mimosa-video-", suffix);
                Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
                temp.toFile().deleteOnExit();
                return temp.toString();
            }
        } catch (IOException ex) {
            return null;
        }
    }
}