import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SessionLimitDAO {
    public static final int DEFAULT_LIMIT_MINUTES = 60;

    public int getLimitMinutes(int userId, int gameId) throws SQLException {
        String sql = "SELECT limit_minutes FROM session_limits WHERE user_id=? AND game_id=?";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, gameId);
            try (ResultSet result = statement.executeQuery()) { return result.next() ? result.getInt(1) : DEFAULT_LIMIT_MINUTES; }
        }
    }

    public void saveLimit(int userId, int gameId, int minutes) throws SQLException {
        if (minutes < 1 || minutes > 1440) throw new IllegalArgumentException("Limit must be between 1 and 1,440 minutes.");
        String sql = "INSERT INTO session_limits (user_id, game_id, limit_minutes) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE limit_minutes=VALUES(limit_minutes)";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, gameId);
            statement.setInt(3, minutes);
            statement.executeUpdate();
        }
    }

    public List<LimitRecord> findAllLimits() throws SQLException {
        List<LimitRecord> records = new ArrayList<>();
        String sql = "SELECT sl.user_id,u.username,sl.game_id,g.game_name,sl.limit_minutes FROM session_limits sl JOIN users u ON sl.user_id=u.user_id JOIN games g ON sl.game_id=g.game_id ORDER BY u.username,g.game_name";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet result = statement.executeQuery()) {
            while (result.next()) records.add(new LimitRecord(result.getInt("user_id"), result.getString("username"), result.getInt("game_id"), result.getString("game_name"), result.getInt("limit_minutes")));
        }
        return records;
    }

    public static class LimitRecord {
        private final int userId, gameId, minutes;
        private final String username, gameName;
        public LimitRecord(int userId, String username, int gameId, String gameName, int minutes) { this.userId=userId; this.username=username; this.gameId=gameId; this.gameName=gameName; this.minutes=minutes; }
        public int getUserId() { return userId; }
        public int getGameId() { return gameId; }
        public int getMinutes() { return minutes; }
        public String getUsername() { return username; }
        public String getGameName() { return gameName; }
    }
}
