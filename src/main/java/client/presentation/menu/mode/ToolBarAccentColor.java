package client.presentation.menu.mode;

import client.presentation.menu.Menu;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.icons.FlatAbstractIcon;
import com.formdev.flatlaf.util.ColorFunctions;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;

public class ToolBarAccentColor extends JPanel {

    private final Menu menu;
    private final JPopupMenu popup = new JPopupMenu();
    private final String[] accentColorKeys = {
            "App.accent.default", "App.accent.blue", "App.accent.purple", "App.accent.red",
            "App.accent.orange", "App.accent.yellow", "App.accent.green"
    };
    private boolean menuFull = true;
    private JToolBar toolbar;
    private JToggleButton selectedButton;

    public ToolBarAccentColor(Menu menu) {
        this.menu = menu;
        init();
    }

    public void setMenuFull(boolean menuFull) {
        this.menuFull = menuFull;
        removeAll();
        if (menuFull) {
            add(toolbar);
            popup.remove(toolbar);
        } else {
            add(selectedButton);
            popup.add(toolbar);
        }
        SwingUtilities.updateComponentTreeUI(this);
    }

    public boolean isMenuFull() {
        return menuFull;
    }

    public void show(Component com, int x, int y) {
        if (menu.getComponentOrientation().isLeftToRight()) {
            popup.show(com, x, y);
        } else {
            int px = toolbar.getPreferredSize().width + UIScale.scale(5);
            popup.show(com, -px, y);
        }
        SwingUtilities.updateComponentTreeUI(popup);
    }

    private void init() {
        setLayout(new BorderLayout());
        toolbar = new JToolBar();
        add(toolbar);
        putClientProperty(FlatClientProperties.STYLE, "background:$Menu.background");
        toolbar.putClientProperty(FlatClientProperties.STYLE, "background:$Menu.background");
        popup.putClientProperty(FlatClientProperties.STYLE, "background:$Menu.background;borderColor:$Menu.background;");

        ButtonGroup group = new ButtonGroup();
        selectedButton = new JToggleButton(new AccentColorIcon(accentColorKeys[0]));
        selectedButton.addActionListener((ActionEvent e) -> {
            int y = (selectedButton.getPreferredSize().height - (toolbar.getPreferredSize().height + UIScale.scale(10))) / 2;
            show(ToolBarAccentColor.this, getWidth() + UIScale.scale(4), y);
        });

        for (String key : accentColorKeys) {
            JToggleButton button = new JToggleButton(new AccentColorIcon(key));
            Color currentAccent = UIManager.getColor("Component.accentColor");
            Color target = UIManager.getColor(key);
            button.setSelected(currentAccent != null && currentAccent.equals(target));
            button.addActionListener(e -> colorAccentChanged(key));
            group.add(button);
            toolbar.add(button);
        }
    }

    private void colorAccentChanged(String colorKey) {
        if (popup.isVisible()) {
            popup.setVisible(false);
        }
        Color color = UIManager.getColor(colorKey);
        if (color == null) {
            return;
        }
        selectedButton.setIcon(new AccentColorIcon(colorKey));
        Class<? extends LookAndFeel> lafClass = UIManager.getLookAndFeel().getClass();
        try {
            FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", toHexCode(color)));
            FlatLaf.setup(lafClass.getDeclaredConstructor().newInstance());
            FlatLaf.updateUI();
        } catch (Exception ex) {
            LoggingFacade.INSTANCE.logSevere(null, ex);
        }
    }

    private String toHexCode(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private final class AccentColorIcon extends FlatAbstractIcon {
        private final String colorKey;

        private AccentColorIcon(String colorKey) {
            super(16, 16, null);
            this.colorKey = colorKey;
        }

        @Override
        protected void paintIcon(Component c, Graphics2D g) {
            Color accColor = UIManager.getColor(colorKey);
            if (accColor == null) {
                accColor = Color.LIGHT_GRAY;
            } else if (!c.isEnabled()) {
                accColor = FlatLaf.isLafDark()
                        ? ColorFunctions.shade(accColor, 0.5f)
                        : ColorFunctions.tint(accColor, 0.6f);
            }
            g.setColor(accColor);
            g.fillRoundRect(1, 1, width - 2, height - 2, 5, 5);
        }
    }
}
