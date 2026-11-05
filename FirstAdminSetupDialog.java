import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/** First-run bootstrap: ensures the first administrator is created intentionally. */
public class FirstAdminSetupDialog extends JDialog {
    private boolean created;
    public FirstAdminSetupDialog(java.awt.Frame owner,UserDAO userDAO) {
        super(owner,"Initial GamePulse Administrator",true);
        getContentPane().setBackground(AppTheme.BACKGROUND); setLayout(new BorderLayout(10,10));
        JLabel message=new JLabel("No administrator account exists. Create the first admin account to manage GamePulse.");
        message.setForeground(AppTheme.TEXT); message.setBorder(BorderFactory.createEmptyBorder(16,16,0,16)); add(message,BorderLayout.NORTH);
        JTextField username=new JTextField(18); JPasswordField password=new JPasswordField(18), confirm=new JPasswordField(18);
        JPanel fields=new JPanel(new GridLayout(3,2,8,8)); fields.setBackground(AppTheme.BACKGROUND); fields.setBorder(BorderFactory.createEmptyBorder(10,16,10,16));
        fields.add(label("Admin username:")); fields.add(username); fields.add(label("Password:")); fields.add(password); fields.add(label("Confirm password:")); fields.add(confirm); add(fields,BorderLayout.CENTER);
        JPanel buttons=new JPanel(); buttons.setBackground(AppTheme.BACKGROUND); JButton create=new JButton("CREATE ADMIN"); AppTheme.styleButton(create,AppTheme.ACCENT); buttons.add(create); add(buttons,BorderLayout.SOUTH);
        create.addActionListener(e -> {
            char[] a=password.getPassword(),b=confirm.getPassword();
            try {
                if (!Arrays.equals(a,b)) { JOptionPane.showMessageDialog(this,"Passwords do not match."); return; }
                userDAO.registerUser(username.getText().trim(),a,"ADMIN"); created=true; dispose();
            } catch (Exception ex) { UiUtil.showError(this,ex); }
            finally { Arrays.fill(a,'\0'); Arrays.fill(b,'\0'); }
        });
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); pack(); setResizable(false); setLocationRelativeTo(owner);
    }
    private JLabel label(String text) { JLabel l=new JLabel(text); l.setForeground(AppTheme.TEXT); return l; }
    public boolean wasCreated() { return created; }
}
