package client.presentation.login.components;

import client.presentation.login.utils.RippleEffect;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class HeaderButton extends JButton {

    private final RippleEffect rippleEffect;

    public HeaderButton(String text) {
        super(text);
        rippleEffect = new RippleEffect(this);
        init();
    }

    private void init() {
        setContentAreaFilled(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        putClientProperty(FlatClientProperties.STYLE,
                "font:bold +3;borderWidth:0;focusWidth:0;innerFocusWidth:0;arc:20");
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        int arc = UIScale.scale(20);
        rippleEffect.render(g, new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), arc, arc));
    }
}
