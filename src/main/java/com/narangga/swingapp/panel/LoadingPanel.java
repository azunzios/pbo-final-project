package com.narangga.swingapp.panel;

import javax.swing.*;
import java.awt.*;

public class LoadingPanel extends JPanel {
    private JLabel loadingLabel;
    private Timer dotTimer;
    private int dotCount = 0;
    private final LoadingCircle loadingCircle;

    public LoadingPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 5, 5, 5);

        // loading texxt
        loadingLabel = new JLabel("Loading");
        loadingLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        add(loadingLabel, gbc);

        // animasi loading circle
        loadingCircle = new LoadingCircle();
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 5, 5, 5);
        add(loadingCircle, gbc);

        // pengaturan waktunya
        dotTimer = new Timer(500, e -> {
            dotCount = (dotCount + 1) % 4;
            StringBuilder dots = new StringBuilder();
            for (int i = 0; i < dotCount; i++) {
                dots.append(".");
            }
            loadingLabel.setText("Loading" + dots.toString());
        });
        dotTimer.start();
    }

    private class LoadingCircle extends JPanel {
        private Timer rotationTimer;
        private int angle = 0;

        LoadingCircle() {
            setPreferredSize(new Dimension(40, 40));
            setOpaque(false);
            rotationTimer = new Timer(50, e -> {
                angle = (angle + 10) % 360;
                repaint();
            });
            rotationTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            int radius = Math.min(centerX, centerY) - 5;

            g2d.rotate(Math.toRadians(angle), centerX, centerY);
            g2d.setColor(new Color(0, 120, 215));
            g2d.setStroke(new BasicStroke(3));
            g2d.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 0, 270);

            g2d.dispose();
        }
    }

    public void stopAnimation() {
        if (dotTimer != null) {
            dotTimer.stop();
        }
        if (loadingCircle.rotationTimer != null) {
            loadingCircle.rotationTimer.stop();
        }
    }
}