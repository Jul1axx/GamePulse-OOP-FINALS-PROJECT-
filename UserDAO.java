import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Database operations for user accounts. */
public class UserDAO {
    public User authenticate(String username, char[] password) throws SQLException {
        String sql = "SELECT user_id, username, password_hash, password_salt, role FROM users WHERE username = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return null;
                String storedHash = result.getString("password_hash");
                String storedSalt = result.getString("password_salt");
                if (PasswordUtil.verifyPassword(password, storedSalt, storedHash)) {
                    return new User(result.getInt("user_id"), result.getString("username"), result.getString("role"));
                }
            }
        }
        return null;
    }

    /** Creates an account. Public registration should always pass USER. */
    public void registerUser(String username, char[] password, String role) throws SQLException {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username cannot be empty.");
        if (username.length() > 80) throw new IllegalArgumentException("Username must be 80 characters or fewer.");
        if (password == null || password.length < 4) throw new IllegalArgumentException("Password must contain at least 4 characters.");
        String normalizedRole = role == null ? "USER" : role.toUpperCase(Locale.ROOT);
        if (!normalizedRole.equals("ADMIN") && !normalizedRole.equals("USER")) throw new IllegalArgumentException("Invalid user role.");

        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(password, salt);
        String sql = "INSERT INTO users (username, password_hash, password_salt, role) VALUES (?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username.trim());
            statement.setString(2, hash);
            statement.setString(3, salt);
            statement.setString(4, normalizedRole);
            statement.executeUpdate();
        }
    }

    public List<User> findAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, role FROM users ORDER BY username";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) users.add(new User(result.getInt("user_id"), result.getString("username"), result.getString("role")));
        }
        return users;
    }

    public void updateRole(int userId, String role) throws SQLException {
        String normalized = role == null ? "" : role.toUpperCase(Locale.ROOT);
        if (!normalized.equals("ADMIN") && !normalized.equals("USER")) throw new IllegalArgumentException("Invalid role.");
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE users SET role = ? WHERE user_id = ?")) {
            statement.setString(1, normalized);
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
    }

    public void deleteUser(int userId) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE user_id = ?")) {
            statement.setInt(1, userId);
            statement.executeUpdate();
        }
    }

    public int countAdmins() throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM users WHERE UPPER(role)='ADMIN'");
             ResultSet result = statement.executeQuery()) {
            return result.next() ? result.getInt(1) : 0;
        }
    }

    public int countUsers() throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM users");
             ResultSet result = statement.executeQuery()) {
            return result.next() ? result.getInt(1) : 0;
        }
    }
}
