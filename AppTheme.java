import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class AppTheme {
    public static final Color BACKGROUND = new Color(24, 29, 45);
    public static final Color PANEL = new Color(38, 46, 65);
    public static final Color PANEL_LIGHT = new Color(48, 59, 82);
    public static final Color ACCENT = new Color(55, 125, 220);
    public static final Color SUCCESS = new Color(35, 155, 105);
    public static final Color WARNING = new Color(210, 145, 45);
    public static final Color DANGER = new Color(190, 65, 70);
    public static final Color TEXT = new Color(238, 241, 248);
    public static final Color MUTED = new Color(165, 176, 195);
    public static final Color BORDER = new Color(64, 75, 99);

    private AppTheme() { }

    public static void styleButton(JButton button, Color background) {
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBorder(BorderFactory.createEmptyBorder(9, 15, 9, 15));
        button.setOpaque(true);
    }

    public static JLabel heading(String value, int size) {
        JLabel label = new JLabel(value);
        label.setForeground(TEXT);
        label.setFont(new Font("Segoe UI", Font.BOLD, size));
        return label;
    }

    public static JLabel muted(String value) {
        JLabel label = new JLabel(value);
        label.setForeground(MUTED);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return label;
    }

    public static JPanel card(String title, String value) {
        JPanel panel = new JPanel();
        panel.setBackground(PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(15, 17, 15, 17)));
        panel.setLayout(new java.awt.BorderLayout(0, 9));
        JLabel titleLabel = muted(title.toUpperCase());
        JLabel valueLabel = heading(value, 23);
        panel.add(titleLabel, java.awt.BorderLayout.NORTH);
        panel.add(valueLabel, java.awt.BorderLayout.CENTER);
        return panel;
    }

    public static void stylePanel(JPanel panel) { panel.setBackground(BACKGROUND); }
}
