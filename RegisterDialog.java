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

/** Public self-registration creates USER accounts only. */
public class RegisterDialog extends JDialog {
    private final UserDAO userDAO;
    public RegisterDialog(java.awt.Frame owner, UserDAO userDAO) {
        super(owner, "Create GamePulse Account", true);
        this.userDAO=userDAO;
        getContentPane().setBackground(AppTheme.BACKGROUND);
        setLayout(new BorderLayout(12,12));
        JPanel fields=new JPanel(new GridLayout(3,2,10,10));
        fields.setBackground(AppTheme.BACKGROUND);
        fields.setBorder(BorderFactory.createEmptyBorder(18,18,8,18));
        JTextField username=new JTextField(18);
        JPasswordField password=new JPasswordField(18);
        JPasswordField confirm=new JPasswordField(18);
        JLabel u=new JLabel("Username:"); JLabel p=new JLabel("Password:"); JLabel c=new JLabel("Confirm password:");
        for (JLabel label:new JLabel[]{u,p,c}) label.setForeground(AppTheme.TEXT);
        fields.add(u); fields.add(username); fields.add(p); fields.add(password); fields.add(c); fields.add(confirm);
        add(fields,BorderLayout.CENTER);
        JPanel actions=new JPanel(); actions.setBackground(AppTheme.BACKGROUND);
        JButton create=new JButton("CREATE ACCOUNT"); AppTheme.styleButton(create,AppTheme.ACCENT);
        JButton cancel=new JButton("CANCEL"); AppTheme.styleButton(cancel,AppTheme.PANEL_LIGHT);
        actions.add(create); actions.add(cancel); add(actions,BorderLayout.SOUTH);
        create.addActionListener(e -> {
            char[] first=password.getPassword(), second=confirm.getPassword();
            try {
                if (!Arrays.equals(first,second)) { JOptionPane.showMessageDialog(this,"Passwords do not match.","Registration",JOptionPane.WARNING_MESSAGE); return; }
                userDAO.registerUser(username.getText().trim(),first,"USER");
                JOptionPane.showMessageDialog(this,"Account created. You can now log in.","Registration complete",JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (Exception ex) { UiUtil.showError(this,ex); }
            finally { Arrays.fill(first,'\0'); Arrays.fill(second,'\0'); }
        });
        cancel.addActionListener(e -> dispose());
        pack(); setResizable(false); setLocationRelativeTo(owner);
    }
}
