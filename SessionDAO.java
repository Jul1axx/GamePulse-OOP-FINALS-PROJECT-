import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SessionDAO {
    private static final String SELECT_COLUMNS = "s.session_id, s.user_id, u.username, s.game_id, g.game_name, s.start_time, s.end_time, s.duration_seconds, s.limit_minutes_at_start";

    public GameSession startSession(int userId, int gameId) throws SQLException {
        if (findActiveSession(userId) != null) throw new IllegalStateException("You already have an active gaming session. End it before starting another.");
        String sql = "INSERT INTO sessions (user_id, game_id, start_time, limit_minutes_at_start) VALUES (?, ?, NOW(), COALESCE((SELECT limit_minutes FROM session_limits WHERE user_id = ? AND game_id = ?), 60))";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, gameId);
            statement.setInt(3, userId);
            statement.setInt(4, gameId);
            statement.executeUpdate();
        }
        return findActiveSession(userId);
    }

    public GameSession findActiveSession(int userId) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM sessions s JOIN users u ON s.user_id=u.user_id JOIN games g ON s.game_id=g.game_id WHERE s.user_id=? AND s.end_time IS NULL ORDER BY s.start_time DESC LIMIT 1";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet result = statement.executeQuery()) { return result.next() ? map(result) : null; }
        }
    }

    public GameSession endSession(long sessionId, int userId) throws SQLException {
        String sql = "UPDATE sessions SET end_time=NOW(), duration_seconds=TIMESTAMPDIFF(SECOND, start_time, NOW()) WHERE session_id=? AND user_id=? AND end_time IS NULL";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, sessionId);
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
        return findById(sessionId);
    }

    public List<GameSession> findByUser(int userId) throws SQLException {
        return findSessions("WHERE s.user_id=?", userId, 300);
    }

    public List<GameSession> findAllSessions() throws SQLException {
        return findSessions("", null, 500);
    }

    private List<GameSession> findSessions(String condition, Integer userId, int limit) throws SQLException {
        List<GameSession> sessions = new ArrayList<>();
        String sql = "SELECT " + SELECT_COLUMNS + " FROM sessions s JOIN users u ON s.user_id=u.user_id JOIN games g ON s.game_id=g.game_id " + condition + " ORDER BY s.start_time DESC LIMIT " + limit;
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            if (userId != null) statement.setInt(1, userId);
            try (ResultSet result = statement.executeQuery()) { while (result.next()) sessions.add(map(result)); }
        }
        return sessions;
    }

    private GameSession findById(long id) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM sessions s JOIN users u ON s.user_id=u.user_id JOIN games g ON s.game_id=g.game_id WHERE s.session_id=?";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery()) { return result.next() ? map(result) : null; }
        }
    }

    private GameSession map(ResultSet result) throws SQLException {
        Timestamp start = result.getTimestamp("start_time");
        Timestamp end = result.getTimestamp("end_time");
        return new GameSession(result.getLong("session_id"), result.getInt("user_id"), result.getString("username"),
                result.getInt("game_id"), result.getString("game_name"), start.toLocalDateTime(),
                end == null ? null : end.toLocalDateTime(), result.getLong("duration_seconds"), result.getInt("limit_minutes_at_start"));
    }

    public int countAllSessions() throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM sessions");
             ResultSet result = statement.executeQuery()) {
            return result.next() ? result.getInt(1) : 0;
        }
    }

    public int countSessionsToday() throws SQLException {
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM sessions WHERE DATE(start_time)=CURDATE()"); ResultSet result = statement.executeQuery()) {
            return result.next() ? result.getInt(1) : 0;
        }
    }
}
