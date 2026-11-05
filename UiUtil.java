import java.awt.Component;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class UiUtil {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");
    private UiUtil() { }
    public static String formatDuration(long seconds) {
        long hours=seconds/3600, minutes=(seconds%3600)/60, secs=seconds%60;
        return String.format("%02d:%02d:%02d",hours,minutes,secs);
    }
    public static String formatHours(long seconds) { return String.format("%.1f hours",seconds/3600.0); }
    public static String formatDateTime(LocalDateTime time) { return time == null ? "—" : time.format(DATE_TIME); }
    public static DefaultTableModel tableModel(String... columns) {
        return new DefaultTableModel(columns,0) { @Override public boolean isCellEditable(int row,int column) { return false; } };
    }
    public static void styleTable(JTable table) {
        table.setRowHeight(28); table.setForeground(AppTheme.TEXT); table.setBackground(AppTheme.PANEL);
        table.setGridColor(AppTheme.BORDER); table.setSelectionBackground(AppTheme.ACCENT);
        table.setSelectionForeground(java.awt.Color.WHITE); table.setFillsViewportHeight(true);
        table.getTableHeader().setBackground(AppTheme.PANEL_LIGHT);
        table.getTableHeader().setForeground(AppTheme.TEXT);
        table.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
    }
    public static JScrollPane scroll(JTable table) { styleTable(table); JScrollPane pane=new JScrollPane(table); pane.getViewport().setBackground(AppTheme.PANEL); pane.setBorder(javax.swing.BorderFactory.createLineBorder(AppTheme.BORDER)); return pane; }
    public static void showError(Component parent, Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(parent, ex.getMessage(), "GamePulse Error", JOptionPane.ERROR_MESSAGE);
    }
}
