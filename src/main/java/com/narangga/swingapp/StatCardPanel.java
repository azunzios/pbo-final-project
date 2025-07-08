package com.narangga.swingapp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StatCardPanel extends JPanel {
    private String value;

    public StatCardPanel(String title, String value, Color accentColor, ImageIcon imageIcon) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor), // Left accent border
                new EmptyBorder(15, 20, 15, 20)
        ));
        setPreferredSize(new Dimension(220, 100));
        setOpaque(false); // For custom painting

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

    public void setValue(String value) {
        this.value = value;
        // Update the label if needed
        for (Component comp : getComponents()) {
            if (comp instanceof JLabel && ((JLabel) comp).getFont().getSize() == 36) {
                ((JLabel) comp).setText(value);
                break;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Shadow
        g2d.setColor(new Color(0, 0, 0, 30)); // 12% opacity black
        g2d.fillRoundRect(4, 4, getWidth() - 5, getHeight() - 5, 8, 8);

        // Card Background
        g2d.setColor(getBackground());
        g2d.fillRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 8, 8);

        g2d.dispose();
        super.paintComponent(g);
    }
}
