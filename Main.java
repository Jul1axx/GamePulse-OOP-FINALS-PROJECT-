import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        try {
            DatabaseInitializer.initialize();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "GamePulse could not initialize its MySQL database.\n\n"
                    + ex.getMessage() + "\n\nCheck that XAMPP MySQL is running, the gamepulse database exists, and DBConnection settings are correct.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            UserDAO userDAO=new UserDAO();
            try {
                if (userDAO.countAdmins()==0) {
                    FirstAdminSetupDialog setup=new FirstAdminSetupDialog(null,userDAO);
                    setup.setVisible(true);
                    if (!setup.wasCreated()) {
                        JOptionPane.showMessageDialog(null,"An administrator account is required to finish first-time setup. GamePulse will close.");
                        return;
                    }
                }
                new SystemLogin(userDAO).setVisible(true);
            } catch (SQLException ex) {
                UiUtil.showError(null,ex);
            }
        });
    }
}
