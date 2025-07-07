package com.narangga.swingapp;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

// Kelas untuk sentralisasi properti styling (warna, font, border)
public class Theme {
    // Palet Warna (nuansa gelap seperti game/software modern)
    public static final Color BACKGROUND = new Color(0x24292e); // GitHub Dark Background
    public static final Color PANEL_BACKGROUND = new Color(0x2d333b); // Sedikit lebih terang
    public static final Color ACCENT_COLOR = new Color(0x58a6ff); // Biru terang untuk seleksi & highlight
    public static final Color TEXT_COLOR = new Color(0xc9d1d9); // Abu-abu terang untuk teks
    public static final Color TEXT_SECONDARY = new Color(0x8b949e); // Abu-abu lebih redup
    public static final Color BORDER_COLOR = new Color(0x444c56);

    // Font
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 14);

    // Border
    public static final Border BORDER_EMPTY = BorderFactory.createEmptyBorder(15, 15, 15, 15);
    public static final Border BORDER_TITLED = new CompoundBorder(
            BorderFactory.createMatteBorder(1, 1, 1, 1, BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 10, 10, 10));
}
