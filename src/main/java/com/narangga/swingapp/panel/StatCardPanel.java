package com.narangga.swingapp.panel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StatCardPanel extends JPanel {
    private String value;
    public StatCardPanel(String title, String value, Color accentColor, ImageIcon imageIcon) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                new EmptyBorder(15, 20, 15, 20)
        ));
        setPreferredSize(new Dimension(220, 100));
        setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(74, 74, 74));

        this.value = value;

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(new Color(74, 74, 74));
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        add(titleLabel, BorderLayout.NORTH);
        add(valueLabel, BorderLayout.CENTER);
    }


    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //shadow
        g2d.setColor(new Color(0, 0, 0, 30));
        g2d.fillRoundRect(4, 4, getWidth() - 5, getHeight() - 5, 8, 8);

        //background
        g2d.setColor(getBackground());
        g2d.fillRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 8, 8);

        g2d.dispose();
        super.paintComponent(g);
    }
}
