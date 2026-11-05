import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GameDAO {
    public List<Game> findGames(boolean activeOnly) throws SQLException {
        List<Game> games = new ArrayList<>();
        String sql = "SELECT game_id, game_name, genre, is_active FROM games " + (activeOnly ? "WHERE is_active = TRUE " : "") + "ORDER BY game_name";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) games.add(new Game(result.getInt("game_id"), result.getString("game_name"), result.getString("genre"), result.getBoolean("is_active")));
        }
        return games;
    }

    public void addGame(String name, String genre) throws SQLException {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Game name is required.");
        String sql = "INSERT INTO games (game_name, genre, is_active) VALUES (?, ?, TRUE)";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name.trim());
            statement.setString(2, genre == null || genre.isBlank() ? "Other" : genre.trim());
            statement.executeUpdate();
        }
    }

    public void updateGame(int gameId, String name, String genre) throws SQLException {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Game name is required.");
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("UPDATE games SET game_name = ?, genre = ? WHERE game_id = ?")) {
            statement.setString(1, name.trim());
            statement.setString(2, genre == null || genre.isBlank() ? "Other" : genre.trim());
            statement.setInt(3, gameId);
            statement.executeUpdate();
        }
    }

    public void setActive(int gameId, boolean active) throws SQLException {
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("UPDATE games SET is_active = ? WHERE game_id = ?")) {
            statement.setBoolean(1, active);
            statement.setInt(2, gameId);
            statement.executeUpdate();
        }
    }

    public int countActiveGames() throws SQLException {
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM games WHERE is_active = TRUE"); ResultSet result = statement.executeQuery()) {
            return result.next() ? result.getInt(1) : 0;
        }
    }
}
