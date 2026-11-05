import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class AnalyticsService {
    public AnalyticsData getUserAnalytics(int userId) throws SQLException {
        long today = 0, week = 0, average = 0;
        int exceeded = 0;
        String aggregateSql = "SELECT "
                + "COALESCE(SUM(CASE WHEN DATE(start_time)=CURDATE() THEN duration_seconds ELSE 0 END),0) AS today_seconds, "
                + "COALESCE(SUM(CASE WHEN start_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) THEN duration_seconds ELSE 0 END),0) AS week_seconds, "
                + "COALESCE(AVG(CASE WHEN end_time IS NOT NULL THEN duration_seconds END),0) AS average_seconds, "
                + "COALESCE(SUM(CASE WHEN end_time IS NOT NULL AND duration_seconds >= limit_minutes_at_start*60 THEN 1 ELSE 0 END),0) AS exceeded "
                + "FROM sessions WHERE user_id=?";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(aggregateSql)) {
            statement.setInt(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    today=result.getLong("today_seconds"); week=result.getLong("week_seconds");
                    average=result.getLong("average_seconds"); exceeded=result.getInt("exceeded");
                }
            }
        }
        String topGame = "No data yet";
        String topSql = "SELECT g.game_name FROM sessions s JOIN games g ON s.game_id=g.game_id WHERE s.user_id=? AND s.end_time IS NOT NULL GROUP BY g.game_id,g.game_name ORDER BY SUM(s.duration_seconds) DESC LIMIT 1";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(topSql)) {
            statement.setInt(1, userId);
            try (ResultSet result = statement.executeQuery()) { if (result.next()) topGame=result.getString(1); }
        }
        return new AnalyticsData(today, week, average, topGame, exceeded);
    }

    public Map<String, Long> getLastSevenDays(int userId) throws SQLException {
        Map<String, Long> data = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE");
        LocalDate today = LocalDate.now();
        for (int i=6; i>=0; i--) data.put(today.minusDays(i).format(formatter), 0L);
        String sql = "SELECT DATE(start_time) AS play_date, SUM(duration_seconds) AS total_seconds FROM sessions WHERE user_id=? AND start_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) AND end_time IS NOT NULL GROUP BY DATE(start_time) ORDER BY play_date";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    LocalDate date = result.getDate("play_date").toLocalDate();
                    data.put(date.format(formatter), result.getLong("total_seconds"));
                }
            }
        }
        return data;
    }

    public String buildRecommendation(AnalyticsData data) {
        double todayHours = data.getTodaySeconds() / 3600.0;
        double weekHours = data.getWeekSeconds() / 3600.0;
        double averageMinutes = data.getAverageSeconds() / 60.0;
        if (todayHours >= 4) return "You have played for more than four hours today. Consider stopping for the day and prioritizing sleep, schoolwork, or physical activity.";
        if (averageMinutes >= 120) return "Your average gaming session is two hours or longer. Try splitting longer sessions into shorter blocks with breaks between them.";
        if (weekHours >= 20) return "Your tracked playtime has passed 20 hours this week. Review your schedule and reserve time for school, rest, and other activities.";
        if (data.getExceededSessions() > 0) return "Some sessions exceeded their saved limits. Consider lowering your limits or setting a reminder to take a break.";
        return "Your tracked sessions are within the configured limits. Keep reviewing your weekly playtime and make room for regular breaks.";
    }
}
