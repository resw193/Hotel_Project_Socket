package client.presentation.menu.mode;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LightDarkMode extends JPanel {

    private boolean menuFull = true;
    private JButton buttonLight;
    private JButton buttonDark;
    private JButton buttonLightDark;

    public LightDarkMode() {
        init();
    }

    public void setMenuFull(boolean menuFull) {
        this.menuFull = menuFull;
        if (menuFull) {
            buttonLight.setVisible(true);
            buttonDark.setVisible(true);
            buttonLightDark.setVisible(false);
        } else {
            buttonLight.setVisible(false);
            buttonDark.setVisible(false);
            buttonLightDark.setVisible(true);
        }
    }

    private void init() {
        setBorder(new EmptyBorder(2, 2, 2, 2));
        setLayout(new LightDarkModeLayout());
        putClientProperty(FlatClientProperties.STYLE,
                "arc:999;background:$Menu.lightdark.background");

        buttonLight = new JButton("Light", loadSvg("/presentation/menu/mode/light.svg", "/client/presentation/menu/mode/light.svg"));
        buttonDark = new JButton("Dark", loadSvg("/presentation/menu/mode/dark.svg", "/client/presentation/menu/mode/dark.svg"));
        buttonLightDark = new JButton();
        buttonLightDark.putClientProperty(FlatClientProperties.STYLE,
                "arc:999;background:$Menu.lightdark.button.background;foreground:$Menu.foreground;focusWidth:0;borderWidth:0;innerFocusWidth:0");
        buttonLightDark.addActionListener((ActionEvent e) -> changeMode(!FlatLaf.isLafDark()));
        buttonDark.addActionListener((ActionEvent e) -> changeMode(true));
        buttonLight.addActionListener((ActionEvent e) -> changeMode(false));
        checkStyle();

        add(buttonLight);
        add(buttonDark);
        add(buttonLightDark);
    }

    private Icon loadSvg(String... paths) {
        for (String path : paths) {
            if (getClass().getResource(path) != null) {
                return new FlatSVGIcon(getClass().getResource(path));
            }
        }
        return UIManager.getIcon("OptionPane.informationIcon");
    }

    private void changeMode(boolean dark) {
        if (FlatLaf.isLafDark() == dark) {
            return;
        }
        EventQueue.invokeLater(() -> {
            FlatAnimatedLafChange.showSnapshot();
            if (dark) {
                FlatMacDarkLaf.setup();
            } else {
                FlatMacLightLaf.setup();
            }
            FlatLaf.updateUI();
            checkStyle();
            FlatAnimatedLafChange.hideSnapshotWithAnimation();
        });
    }

    private void checkStyle() {
        boolean isDark = FlatLaf.isLafDark();
        addStyle(buttonLight, !isDark);
        addStyle(buttonDark, isDark);
        buttonLightDark.setIcon(loadSvg(
                isDark ? "/presentation/menu/mode/dark.svg" : "/presentation/menu/mode/light.svg",
                isDark ? "/client/presentation/menu/mode/dark.svg" : "/client/presentation/menu/mode/light.svg"
        ));
    }

    private void addStyle(JButton button, boolean selected) {
        if (selected) {
            button.putClientProperty(FlatClientProperties.STYLE,
                    "arc:999;background:$Menu.lightdark.button.background;foreground:$Menu.foreground;focusWidth:0;borderWidth:0;innerFocusWidth:0");
        } else {
            button.putClientProperty(FlatClientProperties.STYLE,
                    "arc:999;background:null;foreground:$Menu.foreground;focusWidth:0;borderWidth:0;innerFocusWidth:0");
        }
    }

    private final class LightDarkModeLayout implements LayoutManager {
        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return new Dimension(5, buttonDark.getPreferredSize().height + (menuFull ? 0 : 5));
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return new Dimension(0, 0);
        }

        @Override
        public void layoutContainer(Container parent) {
            synchronized (parent.getTreeLock()) {
                Insets insets = parent.getInsets();
                int width = parent.getWidth() - (insets.left + insets.right);
                int height = parent.getHeight() - (insets.top + insets.bottom);
                if (menuFull) {
                    int buttonWidth = width / 2;
                    buttonLight.setBounds(insets.left, insets.top, buttonWidth, height);
                    buttonDark.setBounds(insets.left + buttonWidth, insets.top, width - buttonWidth, height);
                    buttonLightDark.setBounds(0, 0, 0, 0);
                } else {
                    buttonLight.setBounds(0, 0, 0, 0);
                    buttonDark.setBounds(0, 0, 0, 0);
                    buttonLightDark.setBounds(insets.left, insets.top, width, height);
                }
            }
        }
    }
}
