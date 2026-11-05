import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

public class UserDashboard extends JFrame {
    private final User user;
    private final UserDAO userDAO;
    private final GameDAO gameDAO=new GameDAO();
    private final SessionDAO sessionDAO=new SessionDAO();
    private final SessionLimitDAO limitDAO=new SessionLimitDAO();
    private final AnalyticsService analyticsService=new AnalyticsService();

    private final JComboBox<Game> trackerGameCombo=new JComboBox<>();
    private final JLabel timerLabel=AppTheme.heading("00:00:00",42);
    private final JLabel trackerStatus=AppTheme.muted("Choose a game and start a session.");
    private final JLabel trackerLimit=AppTheme.muted("Session limit: —");
    private final JButton startButton=new JButton("START SESSION");
    private final JButton endButton=new JButton("END SESSION");
    private final JTable historyTable=new JTable(UiUtil.tableModel("Game","Started","Ended","Duration","Limit","Status"));
    private final JComboBox<Game> limitGameCombo=new JComboBox<>();
    private final JSpinner limitSpinner=new JSpinner(new SpinnerNumberModel(60,1,1440,5));
    private final JLabel todayValue=AppTheme.heading("—",22), weekValue=AppTheme.heading("—",22), averageValue=AppTheme.heading("—",22), topGameValue=AppTheme.heading("—",22), exceededValue=AppTheme.heading("—",22);
    private final JLabel analyticsTodayValue=AppTheme.heading("—",22), analyticsWeekValue=AppTheme.heading("—",22), analyticsExceededValue=AppTheme.heading("—",22), analyticsTopGameValue=AppTheme.heading("—",22);
    private final JLabel recommendationLabel=AppTheme.muted("Your personalized gaming insights will appear here.");
    private final WeeklyChartPanel overviewChart=new WeeklyChartPanel();
    private final WeeklyChartPanel analyticsChart=new WeeklyChartPanel();

    private GameSession activeSession;
    private boolean limitAlertShown;
    private final Timer uiTimer;

    public UserDashboard(User user,UserDAO userDAO) {
        this.user=user; this.userDAO=userDAO;
        setTitle("GamePulse | User Dashboard"); setSize(1120,760); setMinimumSize(new java.awt.Dimension(900,650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); getContentPane().setBackground(AppTheme.BACKGROUND); setLayout(new BorderLayout(0,0));
        add(buildHeader(),BorderLayout.NORTH);
        JTabbedPane tabs=new JTabbedPane(); tabs.setBackground(AppTheme.BACKGROUND); tabs.setForeground(AppTheme.TEXT);
        tabs.addTab("Overview",buildOverviewPanel()); tabs.addTab("Session Tracker",buildTrackerPanel());
        tabs.addTab("History",buildHistoryPanel()); tabs.addTab("My Limits",buildLimitsPanel()); tabs.addTab("Analytics",buildAnalyticsPanel());
        add(tabs,BorderLayout.CENTER);
        uiTimer=new Timer(1000,e -> updateTimerDisplay());
        loadGames(); restoreActiveSession(); refreshAll();
        setLocationRelativeTo(null);
    }

    private JPanel buildHeader() {
        JPanel header=new JPanel(new BorderLayout()); header.setBackground(AppTheme.PANEL); header.setBorder(BorderFactory.createEmptyBorder(15,22,15,22));
        JPanel identity=new JPanel(new java.awt.GridLayout(2,1,0,3)); identity.setOpaque(false);
        identity.add(AppTheme.heading("GamePulse",24)); identity.add(AppTheme.muted("Welcome back, "+user.getUsername()+"  •  USER"));
        JButton logout=new JButton("LOG OUT"); AppTheme.styleButton(logout,AppTheme.PANEL_LIGHT); logout.addActionListener(e -> { uiTimer.stop(); dispose(); new SystemLogin(userDAO).setVisible(true); });
        header.add(identity,BorderLayout.WEST); header.add(logout,BorderLayout.EAST); return header;
    }

    private JPanel pagePanel() { JPanel panel=new JPanel(new BorderLayout(14,14)); panel.setBackground(AppTheme.BACKGROUND); panel.setBorder(BorderFactory.createEmptyBorder(18,20,20,20)); return panel; }

    private JPanel buildOverviewPanel() {
        JPanel page=pagePanel(); JPanel cards=new JPanel(new GridLayout(1,4,12,12)); cards.setOpaque(false);
        cards.add(statCard("PLAYTIME TODAY",todayValue)); cards.add(statCard("LAST 7 DAYS",weekValue)); cards.add(statCard("AVERAGE SESSION",averageValue)); cards.add(statCard("MOST PLAYED",topGameValue));
        page.add(cards,BorderLayout.NORTH);
        JPanel center=new JPanel(new BorderLayout(12,12)); center.setOpaque(false);
        JPanel chartContainer=new JPanel(new BorderLayout(0,8)); chartContainer.setOpaque(false);
        chartContainer.add(AppTheme.heading("Your weekly playtime",18),BorderLayout.NORTH); chartContainer.add(overviewChart,BorderLayout.CENTER);
        JPanel insight=new JPanel(new BorderLayout(0,8)); insight.setBackground(AppTheme.PANEL); insight.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(AppTheme.BORDER),BorderFactory.createEmptyBorder(16,18,16,18)));
        insight.add(AppTheme.heading("GamePulse insight",17),BorderLayout.NORTH); recommendationLabel.setForeground(AppTheme.TEXT); recommendationLabel.setFont(new java.awt.Font("Segoe UI",java.awt.Font.PLAIN,14)); insight.add(recommendationLabel,BorderLayout.CENTER);
        center.add(chartContainer,BorderLayout.CENTER); center.add(insight,BorderLayout.SOUTH); page.add(center,BorderLayout.CENTER);
        return page;
    }

    private JPanel statCard(String title,JLabel value) {
        JPanel card=new JPanel(new BorderLayout(0,8)); card.setBackground(AppTheme.PANEL); card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(AppTheme.BORDER),BorderFactory.createEmptyBorder(13,15,13,15)));
        card.add(AppTheme.muted(title),BorderLayout.NORTH); card.add(value,BorderLayout.CENTER); return card;
    }

    private JPanel buildTrackerPanel() {
        JPanel page=pagePanel();
        JPanel content=new JPanel(); content.setBackground(AppTheme.PANEL); content.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(AppTheme.BORDER),BorderFactory.createEmptyBorder(25,30,25,30)));
        content.setLayout(new javax.swing.BoxLayout(content,javax.swing.BoxLayout.Y_AXIS));
        JLabel title=AppTheme.heading("Gaming session",24); title.setAlignmentX(CENTER_ALIGNMENT);
        JLabel choose=AppTheme.muted("SELECT GAME"); choose.setAlignmentX(CENTER_ALIGNMENT);
        trackerGameCombo.setMaximumSize(new java.awt.Dimension(360,38)); trackerGameCombo.setAlignmentX(CENTER_ALIGNMENT);
        timerLabel.setAlignmentX(CENTER_ALIGNMENT);
        trackerLimit.setAlignmentX(CENTER_ALIGNMENT); trackerStatus.setAlignmentX(CENTER_ALIGNMENT);
        JPanel buttons=new JPanel(new FlowLayout(FlowLayout.CENTER,12,8)); buttons.setOpaque(false);
        AppTheme.styleButton(startButton,AppTheme.SUCCESS); AppTheme.styleButton(endButton,AppTheme.DANGER); buttons.add(startButton); buttons.add(endButton); buttons.setAlignmentX(CENTER_ALIGNMENT);
        content.add(title); content.add(javax.swing.Box.createVerticalStrut(22)); content.add(choose); content.add(javax.swing.Box.createVerticalStrut(8)); content.add(trackerGameCombo);
        content.add(javax.swing.Box.createVerticalStrut(30)); content.add(timerLabel); content.add(javax.swing.Box.createVerticalStrut(10)); content.add(trackerLimit); content.add(javax.swing.Box.createVerticalStrut(14)); content.add(trackerStatus); content.add(javax.swing.Box.createVerticalStrut(20)); content.add(buttons);
        JLabel note=AppTheme.muted("GamePulse records sessions while the app is open; elapsed time is calculated from saved timestamps."); note.setAlignmentX(CENTER_ALIGNMENT); content.add(javax.swing.Box.createVerticalStrut(22)); content.add(note);
        page.add(content,BorderLayout.CENTER);
        startButton.addActionListener(e -> startSession()); endButton.addActionListener(e -> endSession());
        return page;
    }

    private JPanel buildHistoryPanel() {
        JPanel page=pagePanel(); JPanel top=new JPanel(new BorderLayout()); top.setOpaque(false);
        top.add(AppTheme.heading("Your gaming history",19),BorderLayout.WEST);
        JButton refresh=new JButton("REFRESH"); AppTheme.styleButton(refresh,AppTheme.PANEL_LIGHT); refresh.addActionListener(e -> refreshHistory()); top.add(refresh,BorderLayout.EAST);
        page.add(top,BorderLayout.NORTH); page.add(UiUtil.scroll(historyTable),BorderLayout.CENTER); return page;
    }

    private JPanel buildLimitsPanel() {
        JPanel page=pagePanel(); JPanel form=new JPanel(); form.setBackground(AppTheme.PANEL); form.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(AppTheme.BORDER),BorderFactory.createEmptyBorder(24,24,24,24)));
        form.setLayout(new javax.swing.BoxLayout(form,javax.swing.BoxLayout.Y_AXIS));
        JLabel title=AppTheme.heading("Personal session limits",22); title.setAlignmentX(CENTER_ALIGNMENT); form.add(title); form.add(javax.swing.Box.createVerticalStrut(12));
        JLabel desc=AppTheme.muted("Set a separate playtime target for each game. The default is 60 minutes."); desc.setAlignmentX(CENTER_ALIGNMENT); form.add(desc); form.add(javax.swing.Box.createVerticalStrut(24));
        limitGameCombo.setMaximumSize(new java.awt.Dimension(340,38)); limitGameCombo.setAlignmentX(CENTER_ALIGNMENT); form.add(limitGameCombo); form.add(javax.swing.Box.createVerticalStrut(15));
        JPanel row=new JPanel(new FlowLayout(FlowLayout.CENTER,10,4)); row.setOpaque(false); JLabel label=new JLabel("Limit (minutes):"); label.setForeground(AppTheme.TEXT); row.add(label); row.add(limitSpinner); form.add(row);
        JButton save=new JButton("SAVE LIMIT"); AppTheme.styleButton(save,AppTheme.ACCENT); save.setAlignmentX(CENTER_ALIGNMENT); form.add(javax.swing.Box.createVerticalStrut(15)); form.add(save);
        limitGameCombo.addActionListener(e -> loadSelectedLimit());
        save.addActionListener(e -> saveLimit());
        page.add(form,BorderLayout.NORTH); return page;
    }

    private JPanel buildAnalyticsPanel() {
        JPanel page=pagePanel(); JPanel cards=new JPanel(new GridLayout(1,4,12,12)); cards.setOpaque(false);
        cards.add(statCard("PLAYTIME TODAY",analyticsTodayValue)); cards.add(statCard("LAST 7 DAYS",analyticsWeekValue)); cards.add(statCard("LIMIT EXCEEDED",analyticsExceededValue)); cards.add(statCard("TOP GAME",analyticsTopGameValue));
        page.add(cards,BorderLayout.NORTH);
        JPanel body=new JPanel(new BorderLayout(0,10)); body.setOpaque(false); body.add(AppTheme.heading("Daily playtime — last seven days",18),BorderLayout.NORTH); body.add(analyticsChart,BorderLayout.CENTER);
        page.add(body,BorderLayout.CENTER); return page;
    }

    private void loadGames() {
        try {
            List<Game> games=gameDAO.findGames(true); trackerGameCombo.removeAllItems(); limitGameCombo.removeAllItems();
            for (Game game:games) { trackerGameCombo.addItem(game); limitGameCombo.addItem(game); }
            if (games.isEmpty()) trackerStatus.setText("No active games are available. Ask an administrator to add games.");
            loadSelectedLimit();
        } catch (Exception ex) { UiUtil.showError(this,ex); }
    }

    private void restoreActiveSession() {
        try {
            activeSession=sessionDAO.findActiveSession(user.getUserId());
            if (activeSession!=null) { limitAlertShown=false; uiTimer.start(); trackerStatus.setText("Active session restored: "+activeSession.getGameName()); updateTimerDisplay(); }
        } catch (Exception ex) { UiUtil.showError(this,ex); }
        updateTrackerControls();
    }

    private void startSession() {
        Game game=(Game)trackerGameCombo.getSelectedItem();
        if (game==null) { JOptionPane.showMessageDialog(this,"There are no active games to track.","GamePulse",JOptionPane.WARNING_MESSAGE); return; }
        try {
            activeSession=sessionDAO.startSession(user.getUserId(),game.getGameId());
            limitAlertShown=false; uiTimer.start(); updateTimerDisplay(); updateTrackerControls();
            trackerStatus.setText("Session running: "+game.getName());
        } catch (Exception ex) { UiUtil.showError(this,ex); }
    }

    private void endSession() {
        if (activeSession==null) return;
        try {
            GameSession completed=sessionDAO.endSession(activeSession.getSessionId(),user.getUserId());
            uiTimer.stop(); activeSession=null; limitAlertShown=false; timerLabel.setText("00:00:00");
            trackerStatus.setText(completed==null?"Session ended.":"Session saved: "+UiUtil.formatDuration(completed.getDurationSeconds()));
            updateTrackerControls(); refreshAll();
        } catch (Exception ex) { UiUtil.showError(this,ex); }
    }

    private void updateTimerDisplay() {
        if (activeSession==null) return;
        long elapsed=activeSession.getDurationSeconds(); timerLabel.setText(UiUtil.formatDuration(elapsed));
        long limitSeconds=activeSession.getLimitMinutesAtStart()*60L;
        long remaining=limitSeconds-elapsed;
        if (remaining>0) trackerLimit.setText("Limit: "+activeSession.getLimitMinutesAtStart()+" min • "+UiUtil.formatDuration(remaining)+" remaining");
        else trackerLimit.setText("Limit exceeded by "+UiUtil.formatDuration(-remaining));
        if (elapsed>=limitSeconds && !limitAlertShown) {
            limitAlertShown=true;
            JOptionPane.showMessageDialog(this,"You have reached your "+activeSession.getLimitMinutesAtStart()+"-minute limit for "+activeSession.getGameName()+". Consider ending the session and taking a break.","Session limit reached",JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updateTrackerControls() {
        boolean running=activeSession!=null; startButton.setEnabled(!running && trackerGameCombo.getItemCount()>0); endButton.setEnabled(running); trackerGameCombo.setEnabled(!running);
        if (!running) trackerLimit.setText("Session limit: —");
    }

    private void loadSelectedLimit() {
        Game game=(Game)limitGameCombo.getSelectedItem(); if (game==null) return;
        try { limitSpinner.setValue(limitDAO.getLimitMinutes(user.getUserId(),game.getGameId())); }
        catch (Exception ex) { UiUtil.showError(this,ex); }
    }

    private void saveLimit() {
        Game game=(Game)limitGameCombo.getSelectedItem(); if (game==null) return;
        try {
            int minutes=(Integer)limitSpinner.getValue(); limitDAO.saveLimit(user.getUserId(),game.getGameId(),minutes);
            JOptionPane.showMessageDialog(this,"Your "+game.getName()+" limit is now "+minutes+" minutes. New sessions will use this limit.","Limit saved",JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) { UiUtil.showError(this,ex); }
    }

    private void refreshAll() { refreshHistory(); refreshAnalytics(); }

    private void refreshHistory() {
        DefaultTableModel model=(DefaultTableModel)historyTable.getModel(); model.setRowCount(0);
        try {
            for (GameSession session:sessionDAO.findByUser(user.getUserId())) {
                model.addRow(new Object[]{session.getGameName(),UiUtil.formatDateTime(session.getStartTime()),UiUtil.formatDateTime(session.getEndTime()),UiUtil.formatDuration(session.getDurationSeconds()),session.getLimitMinutesAtStart()+" min",session.isActive()?"ACTIVE":(session.getDurationSeconds()>=session.getLimitMinutesAtStart()*60L?"LIMIT REACHED":"COMPLETED")});
            }
        } catch (Exception ex) { UiUtil.showError(this,ex); }
    }

    private void refreshAnalytics() {
        try {
            AnalyticsData data=analyticsService.getUserAnalytics(user.getUserId());
            todayValue.setText(UiUtil.formatHours(data.getTodaySeconds())); weekValue.setText(UiUtil.formatHours(data.getWeekSeconds()));
            averageValue.setText(UiUtil.formatDuration(data.getAverageSeconds())); topGameValue.setText(data.getTopGame()); exceededValue.setText(String.valueOf(data.getExceededSessions()));
            analyticsTodayValue.setText(UiUtil.formatHours(data.getTodaySeconds())); analyticsWeekValue.setText(UiUtil.formatHours(data.getWeekSeconds()));
            analyticsExceededValue.setText(String.valueOf(data.getExceededSessions())); analyticsTopGameValue.setText(data.getTopGame());
            recommendationLabel.setText("<html><div style='width:650px'>"+analyticsService.buildRecommendation(data)+"</div></html>");
            Map<String,Long> daily=analyticsService.getLastSevenDays(user.getUserId()); overviewChart.setValues(daily); analyticsChart.setValues(daily);
        } catch (Exception ex) { UiUtil.showError(this,ex); }
    }
}
