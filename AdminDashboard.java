import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

public class AdminDashboard extends JFrame {
    private final User admin;
    private final UserDAO userDAO;
    private final GameDAO gameDAO=new GameDAO();
    private final SessionDAO sessionDAO=new SessionDAO();
    private final SessionLimitDAO limitDAO=new SessionLimitDAO();

    private final JTable usersTable=new JTable(UiUtil.tableModel("ID","Username","Role"));
    private final JTable gamesTable=new JTable(UiUtil.tableModel("ID","Game","Genre","Status"));
    private final JTable sessionsTable=new JTable(UiUtil.tableModel("User","Game","Started","Ended","Duration","Limit","Status"));
    private final JTable limitsTable=new JTable(UiUtil.tableModel("User ID","Username","Game ID","Game","Limit (min)"));
    private final JLabel usersCount=AppTheme.heading("—",25), gamesCount=AppTheme.heading("—",25), todaySessions=AppTheme.heading("—",25), sessionsCount=AppTheme.heading("—",25);

    public AdminDashboard(User admin,UserDAO userDAO) {
        this.admin=admin; this.userDAO=userDAO;
        setTitle("GamePulse | Admin Dashboard"); setSize(1180,780); setMinimumSize(new java.awt.Dimension(950,650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); getContentPane().setBackground(AppTheme.BACKGROUND); setLayout(new BorderLayout());
        add(buildHeader(),BorderLayout.NORTH);
        JTabbedPane tabs=new JTabbedPane(); tabs.setBackground(AppTheme.BACKGROUND); tabs.setForeground(AppTheme.TEXT);
        tabs.addTab("Overview",buildOverview()); tabs.addTab("Accounts",buildAccounts()); tabs.addTab("Games",buildGames()); tabs.addTab("Sessions",buildSessions()); tabs.addTab("Limits",buildLimits());
        add(tabs,BorderLayout.CENTER); refreshAll(); setLocationRelativeTo(null);
    }

    private JPanel buildHeader() {
        JPanel header=new JPanel(new BorderLayout()); header.setBackground(AppTheme.PANEL); header.setBorder(BorderFactory.createEmptyBorder(15,22,15,22));
        JPanel left=new JPanel(new GridLayout(2,1,0,3)); left.setOpaque(false); left.add(AppTheme.heading("GamePulse Admin",24)); left.add(AppTheme.muted("Signed in as "+admin.getUsername()+"  •  ADMIN"));
        JButton logout=new JButton("LOG OUT"); AppTheme.styleButton(logout,AppTheme.PANEL_LIGHT); logout.addActionListener(e -> { dispose(); new SystemLogin(userDAO).setVisible(true); });
        header.add(left,BorderLayout.WEST); header.add(logout,BorderLayout.EAST); return header;
    }

    private JPanel page() { JPanel p=new JPanel(new BorderLayout(12,12)); p.setBackground(AppTheme.BACKGROUND); p.setBorder(BorderFactory.createEmptyBorder(18,20,20,20)); return p; }

    private JPanel buildOverview() {
        JPanel p=page(); JPanel cards=new JPanel(new GridLayout(1,4,12,12)); cards.setOpaque(false);
        cards.add(card("REGISTERED USERS",usersCount)); cards.add(card("ACTIVE GAMES",gamesCount)); cards.add(card("SESSIONS TODAY",todaySessions)); cards.add(card("TOTAL SESSIONS",sessionsCount));
        p.add(cards,BorderLayout.NORTH);
        JPanel note=new JPanel(new BorderLayout()); note.setBackground(AppTheme.PANEL); note.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(AppTheme.BORDER),BorderFactory.createEmptyBorder(20,22,20,22)));
        note.add(AppTheme.heading("System administration",18),BorderLayout.NORTH);
        JLabel text=AppTheme.muted("Manage registered accounts and roles, maintain the shared game catalog, review session activity, and configure user-specific gaming limits. GamePulse keeps completed session records for reporting.");
        text.setFont(new java.awt.Font("Segoe UI",java.awt.Font.PLAIN,14)); note.add(text,BorderLayout.CENTER); p.add(note,BorderLayout.CENTER);
        JButton refresh=new JButton("REFRESH OVERVIEW"); AppTheme.styleButton(refresh,AppTheme.PANEL_LIGHT); refresh.addActionListener(e -> refreshOverview()); p.add(refresh,BorderLayout.SOUTH);
        return p;
    }

    private JPanel card(String title,JLabel value) {
        JPanel p=new JPanel(new BorderLayout(0,8)); p.setBackground(AppTheme.PANEL); p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(AppTheme.BORDER),BorderFactory.createEmptyBorder(15,16,15,16)));
        p.add(AppTheme.muted(title),BorderLayout.NORTH); p.add(value,BorderLayout.CENTER); return p;
    }

    private JPanel buildAccounts() {
        JPanel p=page(); p.add(UiUtil.scroll(usersTable),BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false);
        JButton add=new JButton("ADD ACCOUNT"), role=new JButton("CHANGE ROLE"), delete=new JButton("DELETE ACCOUNT"), refresh=new JButton("REFRESH");
        AppTheme.styleButton(add,AppTheme.ACCENT); AppTheme.styleButton(role,AppTheme.PANEL_LIGHT); AppTheme.styleButton(delete,AppTheme.DANGER); AppTheme.styleButton(refresh,AppTheme.PANEL_LIGHT);
        actions.add(add); actions.add(role); actions.add(delete); actions.add(refresh); p.add(actions,BorderLayout.NORTH);
        add.addActionListener(e -> addAccount()); role.addActionListener(e -> changeRole()); delete.addActionListener(e -> deleteAccount()); refresh.addActionListener(e -> refreshUsers());
        return p;
    }

    private JPanel buildGames() {
        JPanel p=page(); p.add(UiUtil.scroll(gamesTable),BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false);
        JButton add=new JButton("ADD GAME"), edit=new JButton("EDIT GAME"), toggle=new JButton("ACTIVATE / DEACTIVATE"), refresh=new JButton("REFRESH");
        AppTheme.styleButton(add,AppTheme.ACCENT); AppTheme.styleButton(edit,AppTheme.PANEL_LIGHT); AppTheme.styleButton(toggle,AppTheme.WARNING); AppTheme.styleButton(refresh,AppTheme.PANEL_LIGHT);
        actions.add(add); actions.add(edit); actions.add(toggle); actions.add(refresh); p.add(actions,BorderLayout.NORTH);
        add.addActionListener(e -> addGame()); edit.addActionListener(e -> editGame()); toggle.addActionListener(e -> toggleGame()); refresh.addActionListener(e -> refreshGames());
        return p;
    }

    private JPanel buildSessions() {
        JPanel p=page(); p.add(UiUtil.scroll(sessionsTable),BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false);
        JButton refresh=new JButton("REFRESH SESSIONS"); AppTheme.styleButton(refresh,AppTheme.PANEL_LIGHT); refresh.addActionListener(e -> refreshSessions());
        actions.add(refresh); p.add(actions,BorderLayout.NORTH); return p;
    }

    private JPanel buildLimits() {
        JPanel p=page(); p.add(UiUtil.scroll(limitsTable),BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false);
        JButton set=new JButton("SET USER LIMIT"), edit=new JButton("EDIT SELECTED LIMIT"), refresh=new JButton("REFRESH");
        AppTheme.styleButton(set,AppTheme.ACCENT); AppTheme.styleButton(edit,AppTheme.PANEL_LIGHT); AppTheme.styleButton(refresh,AppTheme.PANEL_LIGHT);
        actions.add(set); actions.add(edit); actions.add(refresh); p.add(actions,BorderLayout.NORTH);
        set.addActionListener(e -> setUserLimit()); edit.addActionListener(e -> editSelectedLimit()); refresh.addActionListener(e -> refreshLimits()); return p;
    }

    private void addAccount() {
        JTextField username=new JTextField(18); JPasswordField password=new JPasswordField(18); JComboBox<String> role=new JComboBox<>(new String[]{"USER","ADMIN"});
        JPanel panel=new JPanel(new GridLayout(3,2,8,8)); panel.add(new JLabel("Username:")); panel.add(username); panel.add(new JLabel("Temporary password:")); panel.add(password); panel.add(new JLabel("Role:")); panel.add(role);
        int choice=JOptionPane.showConfirmDialog(this,panel,"Create account",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
        if (choice!=JOptionPane.OK_OPTION) return;
        char[] chars=password.getPassword();
        try { userDAO.registerUser(username.getText().trim(),chars,(String)role.getSelectedItem()); JOptionPane.showMessageDialog(this,"Account created successfully."); refreshUsers(); }
        catch (Exception ex) { UiUtil.showError(this,ex); }
        finally { java.util.Arrays.fill(chars,'\0'); }
    }

    private void changeRole() {
        int row=usersTable.getSelectedRow(); if (row<0) { warnSelect("account"); return; }
        int userId=((Number)usersTable.getValueAt(row,0)).intValue(); String current=String.valueOf(usersTable.getValueAt(row,2));
        if (userId==admin.getUserId()) { JOptionPane.showMessageDialog(this,"You cannot change your own administrator role while signed in."); return; }
        String selected=(String)JOptionPane.showInputDialog(this,"Choose the new role:","Change role",JOptionPane.QUESTION_MESSAGE,null,new String[]{"USER","ADMIN"},current);
        if (selected==null) return;
        try { userDAO.updateRole(userId,selected); refreshUsers(); }
        catch (Exception ex) { UiUtil.showError(this,ex); }
    }

    private void deleteAccount() {
        int row=usersTable.getSelectedRow(); if (row<0) { warnSelect("account"); return; }
        int userId=((Number)usersTable.getValueAt(row,0)).intValue(); String username=String.valueOf(usersTable.getValueAt(row,1));
        if (userId==admin.getUserId()) { JOptionPane.showMessageDialog(this,"You cannot delete the account currently signed in."); return; }
        int choice=JOptionPane.showConfirmDialog(this,"Delete '"+username+"'? Its sessions and limits will also be deleted.","Confirm deletion",JOptionPane.YES_NO_OPTION);
        if (choice!=JOptionPane.YES_OPTION) return;
        try { userDAO.deleteUser(userId); refreshAll(); }
        catch (Exception ex) { UiUtil.showError(this,ex); }
    }

    private void addGame() {
        JTextField name=new JTextField(18), genre=new JTextField("Other",18);
        JPanel panel=new JPanel(new GridLayout(2,2,8,8)); panel.add(new JLabel("Game name:")); panel.add(name); panel.add(new JLabel("Genre:")); panel.add(genre);
        if (JOptionPane.showConfirmDialog(this,panel,"Add game",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
            try { gameDAO.addGame(name.getText(),genre.getText()); refreshGames(); }
            catch (Exception ex) { UiUtil.showError(this,ex); }
        }
    }

    private void editGame() {
        int row=gamesTable.getSelectedRow(); if (row<0) { warnSelect("game"); return; }
        int id=((Number)gamesTable.getValueAt(row,0)).intValue(); JTextField name=new JTextField(String.valueOf(gamesTable.getValueAt(row,1)),18); JTextField genre=new JTextField(String.valueOf(gamesTable.getValueAt(row,2)),18);
        JPanel panel=new JPanel(new GridLayout(2,2,8,8)); panel.add(new JLabel("Game name:")); panel.add(name); panel.add(new JLabel("Genre:")); panel.add(genre);
        if (JOptionPane.showConfirmDialog(this,panel,"Edit game",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
            try { gameDAO.updateGame(id,name.getText(),genre.getText()); refreshGames(); }
            catch (Exception ex) { UiUtil.showError(this,ex); }
        }
    }

    private void toggleGame() {
        int row=gamesTable.getSelectedRow(); if (row<0) { warnSelect("game"); return; }
        int id=((Number)gamesTable.getValueAt(row,0)).intValue(); boolean active="ACTIVE".equals(gamesTable.getValueAt(row,3));
        try { gameDAO.setActive(id,!active); refreshGames(); }
        catch (Exception ex) { UiUtil.showError(this,ex); }
    }

    private void setUserLimit() {
        try {
            List<User> users=userDAO.findAllUsers(); List<Game> games=gameDAO.findGames(true);
            if (users.isEmpty() || games.isEmpty()) { JOptionPane.showMessageDialog(this,"Create at least one user and one active game first."); return; }
            JComboBox<UserChoice> userChoice=new JComboBox<>(); for (User u:users) userChoice.addItem(new UserChoice(u.getUserId(),u.getUsername()));
            JComboBox<Game> gameChoice=new JComboBox<>(); for (Game g:games) gameChoice.addItem(g);
            JSpinner minutes=new JSpinner(new SpinnerNumberModel(60,1,1440,5));
            JPanel panel=new JPanel(new GridLayout(3,2,8,8)); panel.add(new JLabel("User:")); panel.add(userChoice); panel.add(new JLabel("Game:")); panel.add(gameChoice); panel.add(new JLabel("Limit (minutes):")); panel.add(minutes);
            if (JOptionPane.showConfirmDialog(this,panel,"Set user session limit",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
                UserChoice u=(UserChoice)userChoice.getSelectedItem(); Game g=(Game)gameChoice.getSelectedItem();
                if (u!=null && g!=null) { limitDAO.saveLimit(u.id,g.getGameId(),(Integer)minutes.getValue()); refreshLimits(); }
            }
        } catch (Exception ex) { UiUtil.showError(this,ex); }
    }

    private void editSelectedLimit() {
        int row=limitsTable.getSelectedRow(); if (row<0) { warnSelect("limit"); return; }
        int userId=((Number)limitsTable.getValueAt(row,0)).intValue(); int gameId=((Number)limitsTable.getValueAt(row,2)).intValue(); int current=((Number)limitsTable.getValueAt(row,4)).intValue();
        JSpinner minutes=new JSpinner(new SpinnerNumberModel(current,1,1440,5));
        if (JOptionPane.showConfirmDialog(this,minutes,"New limit in minutes",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
            try { limitDAO.saveLimit(userId,gameId,(Integer)minutes.getValue()); refreshLimits(); }
            catch (Exception ex) { UiUtil.showError(this,ex); }
        }
    }

    private void warnSelect(String item) { JOptionPane.showMessageDialog(this,"Select a "+item+" from the table first.","GamePulse",JOptionPane.INFORMATION_MESSAGE); }
    private void refreshAll() { refreshOverview(); refreshUsers(); refreshGames(); refreshSessions(); refreshLimits(); }

    private void refreshOverview() {
        try {
            usersCount.setText(String.valueOf(userDAO.countUsers())); gamesCount.setText(String.valueOf(gameDAO.countActiveGames()));
            todaySessions.setText(String.valueOf(sessionDAO.countSessionsToday())); sessionsCount.setText(String.valueOf(sessionDAO.countAllSessions()));
        } catch (Exception ex) { UiUtil.showError(this,ex); }
    }
    private void refreshUsers() {
        DefaultTableModel model=(DefaultTableModel)usersTable.getModel(); model.setRowCount(0);
        try { for (User u:userDAO.findAllUsers()) model.addRow(new Object[]{u.getUserId(),u.getUsername(),u.getRole()}); }
        catch (Exception ex) { UiUtil.showError(this,ex); }
    }
    private void refreshGames() {
        DefaultTableModel model=(DefaultTableModel)gamesTable.getModel(); model.setRowCount(0);
        try { for (Game g:gameDAO.findGames(false)) model.addRow(new Object[]{g.getGameId(),g.getName(),g.getGenre(),g.isActive()?"ACTIVE":"INACTIVE"}); }
        catch (Exception ex) { UiUtil.showError(this,ex); }
    }
    private void refreshSessions() {
        DefaultTableModel model=(DefaultTableModel)sessionsTable.getModel(); model.setRowCount(0);
        try { for (GameSession s:sessionDAO.findAllSessions()) model.addRow(new Object[]{s.getUsername(),s.getGameName(),UiUtil.formatDateTime(s.getStartTime()),UiUtil.formatDateTime(s.getEndTime()),UiUtil.formatDuration(s.getDurationSeconds()),s.getLimitMinutesAtStart()+" min",s.isActive()?"ACTIVE":(s.getDurationSeconds()>=s.getLimitMinutesAtStart()*60L?"LIMIT REACHED":"COMPLETED")}); }
        catch (Exception ex) { UiUtil.showError(this,ex); }
    }
    private void refreshLimits() {
        DefaultTableModel model=(DefaultTableModel)limitsTable.getModel(); model.setRowCount(0);
        try { for (SessionLimitDAO.LimitRecord r:limitDAO.findAllLimits()) model.addRow(new Object[]{r.getUserId(),r.getUsername(),r.getGameId(),r.getGameName(),r.getMinutes()}); }
        catch (Exception ex) { UiUtil.showError(this,ex); }
    }

    private static class UserChoice {
        private final int id; private final String username;
        UserChoice(int id,String username) { this.id=id; this.username=username; }
        @Override public String toString() { return username; }
    }
}
