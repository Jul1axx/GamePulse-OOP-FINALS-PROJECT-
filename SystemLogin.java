import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.util.Arrays;
import javax.swing.*;

public class SystemLogin extends JFrame {
    private final UserDAO userDAO;
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JLabel messageLabel = new JLabel(" ", SwingConstants.CENTER);

    public SystemLogin(UserDAO userDAO) {
        this.userDAO=userDAO;
        setTitle("GamePulse | Sign in"); setSize(430,390); setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false); getContentPane().setBackground(AppTheme.BACKGROUND); setLayout(null);
        JLabel title=new JLabel("GamePulse",SwingConstants.CENTER); title.setBounds(45,22,330,42);
        title.setForeground(AppTheme.TEXT); title.setFont(new Font("Segoe UI",Font.BOLD,29)); add(title);
        JLabel subtitle=new JLabel("Track your playtime. Build better balance.",SwingConstants.CENTER);
        subtitle.setBounds(30,65,370,25); subtitle.setForeground(AppTheme.MUTED); add(subtitle);
        JLabel usernameLabel=new JLabel("Username"); usernameLabel.setBounds(55,112,100,22); usernameLabel.setForeground(AppTheme.TEXT); add(usernameLabel);
        usernameField.setBounds(55,136,320,31); add(usernameField);
        JLabel passwordLabel=new JLabel("Password"); passwordLabel.setBounds(55,180,100,22); passwordLabel.setForeground(AppTheme.TEXT); add(passwordLabel);
        passwordField.setBounds(55,204,320,31); add(passwordField);
        JButton login=new JButton("LOG IN"); login.setBounds(55,253,155,36); AppTheme.styleButton(login,AppTheme.ACCENT); add(login);
        JButton register=new JButton("CREATE ACCOUNT"); register.setBounds(220,253,155,36); AppTheme.styleButton(register,AppTheme.PANEL_LIGHT); add(register);
        messageLabel.setBounds(40,300,350,28); messageLabel.setForeground(AppTheme.MUTED); add(messageLabel);
        login.addActionListener(e -> login());
        register.addActionListener(e -> new RegisterDialog(this,userDAO).setVisible(true));
        passwordField.addActionListener(e -> login());
        File iconFile=new File("GamePulse.png"); if (!iconFile.exists()) iconFile=new File("DATAFiles/GamePulse.png");
        if (iconFile.exists()) setIconImage(new ImageIcon(iconFile.getPath()).getImage());
        getRootPane().setDefaultButton(login); setLocationRelativeTo(null);
    }

    private void login() {
        String username=usernameField.getText().trim(); char[] password=passwordField.getPassword();
        try {
            if (username.isEmpty() || password.length==0) { showMessage("Enter your username and password.",false); return; }
            User user=userDAO.authenticate(username,password);
            if (user==null) { showMessage("Invalid username or password.",false); return; }
            showMessage("Login successful. Opening GamePulse...",true);
            if ("ADMIN".equalsIgnoreCase(user.getRole())) new AdminDashboard(user,userDAO).setVisible(true);
            else new UserDashboard(user,userDAO).setVisible(true);
            dispose();
        } catch (Exception ex) { UiUtil.showError(this,ex); showMessage("Unable to sign in. Check the database connection.",false); }
        finally { Arrays.fill(password,'\0'); passwordField.setText(""); }
    }

    private void showMessage(String message,boolean success) {
        messageLabel.setText(message); messageLabel.setForeground(success?new Color(100,220,155):new Color(255,130,130));
    }
}
