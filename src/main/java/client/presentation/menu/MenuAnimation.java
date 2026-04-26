package client.presentation.menu;

import com.formdev.flatlaf.util.Animator;

import java.awt.*;
import java.util.HashMap;
import java.util.Random;

public class MenuAnimation {

    private static final HashMap<MenuItem, Animator> HASH = new HashMap<>();
    private static final Random RANDOM = new Random();

    public static void animate(MenuItem menu, boolean show) {
        if (HASH.containsKey(menu) && HASH.get(menu).isRunning()) {
            HASH.get(menu).stop();
        }

        Animator lightningAnimator = new Animator(200, new Animator.TimingTarget() {
            private int flashCount = 0;
            private final int maxFlashes = 3;
            private final Color originalBackground = menu.getBackground();
            private boolean isFlashOn = false;

            @Override
            public void timingEvent(float f) {
                if (f >= (flashCount + 1) / (float) maxFlashes) {
                    flashCount++;
                    isFlashOn = !isFlashOn;
                    menu.setBackground(isFlashOn ? new Color(211, 211, 211) : originalBackground);
                }
                int shakeX = RANDOM.nextInt(3) - 1;
                int shakeY = RANDOM.nextInt(3) - 1;
                menu.setLocation(menu.getX() + shakeX, menu.getY() + shakeY);
                menu.revalidate();
                menu.repaint();
            }

            @Override
            public void end() {
                menu.setBackground(originalBackground);
                menu.setLocation(menu.getX() - (menu.getX() % 2), menu.getY() - (menu.getY() % 2));
                menu.revalidate();
                runMenuAnimation(menu, show);
            }
        });
        lightningAnimator.setResolution(1);
        lightningAnimator.start();
        HASH.put(menu, lightningAnimator);
    }

    private static void runMenuAnimation(MenuItem menu, boolean show) {
        menu.setMenuShow(show);
        Animator animator = new Animator(400, new Animator.TimingTarget() {
            @Override
            public void timingEvent(float f) {
                menu.setAnimate(show ? f : 1f - f);
                menu.revalidate();
            }

            @Override
            public void end() {
                HASH.remove(menu);
            }
        });
        animator.setResolution(1);
        animator.setInterpolator(f -> (float) (1 - Math.pow(1 - f, 3)));
        animator.start();
        HASH.put(menu, animator);
    }
}
